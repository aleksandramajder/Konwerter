package com.example.konwerter.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.konwerter.R
import com.example.konwerter.data.Category
import com.example.konwerter.data.Converter
import com.example.konwerter.data.CryptoRepository
import com.example.konwerter.data.ConversionUnit
import com.example.konwerter.data.UnitRepository
import com.example.konwerter.databinding.FragmentConversionBinding
import com.example.konwerter.utils.PreferencesManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversionFragment : Fragment() {

    private var _binding: FragmentConversionBinding? = null
    private val binding get() = _binding!!

    private lateinit var category: Category
    private var units: List<ConversionUnit> = emptyList()
    private var fromUnit: ConversionUnit? = null
    private var toUnit: ConversionUnit? = null
    private var initialLoadRetryCount = 0
    private var isListenersSetup = false
    private var chartJob: Job? = null

    private var lastRefreshTime = 0L
    private var decimalFormat = DecimalFormat("#.########")

    companion object {
        private const val ARG_CATEGORY = "category"
        private const val MIN_REFRESH_INTERVAL = 30000L

        fun newInstance(category: Category): ConversionFragment {
            return ConversionFragment().apply {
                arguments = Bundle().apply { putString(ARG_CATEGORY, category.name) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getString(ARG_CATEGORY)?.let { Category.valueOf(it) } ?: Category.LENGTH
    }

    override fun onResume() {
        super.onResume()
        if (category == Category.CRYPTO) {
            val timeSinceLastLoad = System.currentTimeMillis() - lastRefreshTime
            if (units.isEmpty() || timeSinceLastLoad > MIN_REFRESH_INTERVAL) {
                loadCryptoUnits(isInitialLoad = units.isEmpty())
            } else {
                setupUI()
            }
        } else {
            units = UnitRepository.getUnitsForCategory(category)
            setupUI()
        }
    }

    private fun setupUI() {
        if (_binding == null) return
        updateDecimalFormat()
        setupDropdowns()
        if (!isListenersSetup) {
            setupInputListeners()
        }
        setupCryptoUI()
    }

    private fun updateDecimalFormat() {
        val decimalPlaces = PreferencesManager.getDecimalPlaces(requireContext())
        val pattern = "#." + "#".repeat(decimalPlaces)

        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.decimalSeparator = ','

        decimalFormat = DecimalFormat(pattern, symbols)
    }

    private fun loadCryptoUnits(isInitialLoad: Boolean = false, onComplete: (() -> Unit)? = null) {
        showLoading(true)
        binding.buttonRefresh?.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                units = CryptoRepository.getCryptoUnits()
                if (_binding != null) {
                    showError(false)
                    lastRefreshTime = System.currentTimeMillis()
                    if (isInitialLoad) {
                        initialLoadRetryCount = 0
                    }
                    onComplete?.invoke() ?: setupUI()
                }
            } catch (e: Exception) {
                android.util.Log.e("ConversionFragment", "Błąd ładowania kryptowalut", e)

                if (_binding != null) {
                    if (isInitialLoad) {
                        handleNetworkError()
                    } else {
                        val is429Error = e.message?.contains("429") == true

                        val message = when {
                            is429Error -> "Za dużo zapytań. Poczekaj 30 sekund przed kolejnym odświeżeniem"
                            e is java.net.SocketTimeoutException -> "Przekroczono limit czasu żądania"
                            e is java.net.UnknownHostException -> "Brak połączenia z internetem"
                            e is java.io.IOException -> "Problem z połączeniem. Spróbuj za chwilę"
                            else -> "Nie udało się odświeżyć kursów: ${e.message ?: "Nieznany błąd"}"
                        }
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

                        if (is429Error) {
                            lastRefreshTime = System.currentTimeMillis()
                        }
                    }
                }
            } finally {
                if (_binding != null) {
                    showLoading(false)
                    binding.buttonRefresh?.isEnabled = true
                }
            }
        }
    }

    private fun refreshCryptoUnits() {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastRefresh = currentTime - lastRefreshTime

        if (timeSinceLastRefresh < MIN_REFRESH_INTERVAL) {
            val remainingSeconds = ((MIN_REFRESH_INTERVAL - timeSinceLastRefresh) / 1000).toInt()
            Toast.makeText(
                requireContext(),
                "Poczekaj $remainingSeconds sek. przed ponownym odświeżeniem",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        lastRefreshTime = currentTime
        val fromSymbol = fromUnit?.symbol
        val toSymbol = toUnit?.symbol

        CryptoRepository.clearCache()

        loadCryptoUnits(isInitialLoad = false) {
            fromUnit = units.find { it.symbol == fromSymbol } ?: units.getOrNull(0)
            toUnit = units.find { it.symbol == toSymbol } ?: units.getOrNull(1)

            setupUI()
            Toast.makeText(requireContext(), "Kursy zaktualizowane", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleNetworkError() {
        initialLoadRetryCount++
        android.util.Log.d("ConversionFragment", "Retry count: $initialLoadRetryCount")

        if (initialLoadRetryCount > 5) {
            showError(true, "Przekroczono limit prób. Sprawdź połączenie z internetem.")
            binding.retryButton.isEnabled = false
        } else {
            showError(true, "Brak połączenia z internetem lub limit zapytań API.")
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.mainContent.alpha = if (isLoading) 0.5f else 1.0f
    }

    private fun showError(show: Boolean, message: String = "Brak połączenia z internetem.") {
        binding.errorContainer.visibility = if (show) View.VISIBLE else View.GONE
        binding.mainContent.visibility = if (show) View.GONE else View.VISIBLE
        binding.errorText.text = message
        binding.retryButton.isEnabled = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConversionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (category != Category.CRYPTO) {
            setupUI()
        } else {
            if (!isListenersSetup) {
                setupInputListeners()
            }
        }
    }

    private fun setupDropdowns() {
        if (units.isEmpty()) return

        val unitNames = units.map { "${it.name} (${it.symbol})" }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, unitNames)
        binding.autoCompleteFrom.setAdapter(adapter)
        binding.autoCompleteTo.setAdapter(adapter)

        val fromIndex = units.indexOfFirst { it.symbol == fromUnit?.symbol }.takeIf { it != -1 } ?: 0
        val toIndex = units.indexOfFirst { it.symbol == toUnit?.symbol }.takeIf { it != -1 } ?: if (units.size > 1) 1 else 0

        if (units.isNotEmpty()) {
            binding.autoCompleteFrom.setText(unitNames[fromIndex], false)
            fromUnit = units[fromIndex]

            if (units.size > 1) {
                binding.autoCompleteTo.setText(unitNames[toIndex], false)
                toUnit = units[toIndex]
            }

            if (category == Category.CRYPTO) {
                loadChartData()
            }
        }
    }

    private fun setupInputListeners() {
        if(isListenersSetup) return

        binding.editTextFrom.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performConversion()
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.retryButton.setOnClickListener {
            hideKeyboard()
            binding.retryButton.isEnabled = false

            val timeSinceLastAttempt = System.currentTimeMillis() - lastRefreshTime

            if (timeSinceLastAttempt < MIN_REFRESH_INTERVAL) {
                val remainingSeconds = ((MIN_REFRESH_INTERVAL - timeSinceLastAttempt) / 1000).toInt()
                Toast.makeText(
                    requireContext(),
                    "Poczekaj jeszcze $remainingSeconds sekund",
                    Toast.LENGTH_SHORT
                ).show()
                binding.retryButton.isEnabled = true
            } else {
                initialLoadRetryCount = 0
                loadCryptoUnits(isInitialLoad = true)
            }
        }

        binding.buttonSwap.setOnClickListener {
            hideKeyboard()
            swapUnits()
        }

        binding.buttonRefresh?.setOnClickListener {
            hideKeyboard()
            if (category == Category.CRYPTO) {
                CryptoRepository.clearCache()
                refreshCryptoUnits()
            }
        }

        binding.editTextFrom.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                performConversion()
            }
        })

        binding.autoCompleteFrom.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            hideKeyboard()
            fromUnit = units[position]
            performConversion()
            if (category == Category.CRYPTO) {
                loadChartData()
            }
        }

        binding.autoCompleteTo.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            hideKeyboard()
            toUnit = units[position]
            performConversion()
            if (category == Category.CRYPTO) {
                loadChartData()
            }
        }
        isListenersSetup = true
        binding.editTextFrom.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if ((s?.length ?: 0) >= 25) {
                    binding.textInputLayoutValue.error = "Maksymalna długość to 25 cyfr"
                } else {
                    binding.textInputLayoutValue.error = null
                }
            }
            override fun afterTextChanged(s: Editable?) {
                performConversion()
            }
        })
    }

    private fun setupCryptoUI() {
        if (category == Category.CRYPTO) {
            binding.buttonRefresh?.visibility = View.VISIBLE
            binding.cryptoChart.visibility = View.VISIBLE

            try {
                val typedValue = android.util.TypedValue()
                requireContext().theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
                val themeColor = typedValue.data

                binding.cryptoChart.setNoDataTextColor(themeColor)
                binding.cryptoChart.setNoDataText("Wybierz kryptowalutę, aby zobaczyć wykres")

                binding.cryptoChart.description.isEnabled = false

                binding.cryptoChart.invalidate()
            } catch (e: Exception) {
            }

        } else {
            binding.buttonRefresh?.visibility = View.GONE
            binding.cryptoChart.visibility = View.GONE
        }
    }

    private fun performConversion() {
        val inputText = binding.editTextFrom.text.toString()
        val cleanInput = inputText.replace(',', '.')
        if (fromUnit == null || toUnit == null) {
            binding.textViewResult.text = "0"
            return
        }

        if (category == Category.CRYPTO) {
            binding.buttonSwap.isEnabled = true
            binding.textViewHint.text = "Wpisz wartość aby zobaczyć konwersję"
        }

        if (cleanInput.isEmpty()) {
            binding.textViewResult.text = "0"
            return
        }

        try {
            val inputValue = BigDecimal(cleanInput)
            val result = Converter.convert(inputValue, fromUnit!!, toUnit!!)
            binding.textViewResult.text = decimalFormat.format(result)
        } catch (e: NumberFormatException) {
            binding.textViewResult.text = "Błąd"
        } catch (e: IllegalArgumentException) {
            binding.textViewResult.text = e.message ?: "Błąd"
        }
    }

    private fun loadChartData() {
        chartJob?.cancel()

        if (category != Category.CRYPTO || fromUnit == null || toUnit == null) return
        val typedValue = android.util.TypedValue()

        requireContext().theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
        val themeColor = typedValue.data

        val (crypto, fiat) = when {
            CryptoRepository.isCrypto(fromUnit!!) -> {
                binding.cryptoChart.visibility = View.VISIBLE
                fromUnit!! to toUnit!!
            }
            CryptoRepository.isCrypto(toUnit!!) -> {
                binding.cryptoChart.visibility = View.VISIBLE
                toUnit!! to fromUnit!!
            }
            else -> {
                binding.cryptoChart.visibility = View.GONE
                return
            }
        }

        chartJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val history = CryptoRepository.getHistoricalData(crypto, fiat, "7")

                if (history.isNotEmpty() && _binding != null) {
                    updateChart(history, "${crypto.symbol}/${fiat.symbol}")
                } else if (_binding != null) {
                    if (binding.cryptoChart.data == null || binding.cryptoChart.data.entryCount == 0) {
                        binding.cryptoChart.clear()

                        binding.cryptoChart.setNoDataTextColor(themeColor)
                        binding.cryptoChart.setNoDataText("Brak danych historycznych")

                        binding.cryptoChart.invalidate()
                    } else {
                        Toast.makeText(requireContext(), "Nie udało się odświeżyć wykresu", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (_binding != null) {
                    if (binding.cryptoChart.data == null || binding.cryptoChart.data.entryCount == 0) {
                        binding.cryptoChart.clear()

                        binding.cryptoChart.setNoDataTextColor(themeColor)
                        binding.cryptoChart.setNoDataText("Błąd ładowania wykresu")

                        binding.cryptoChart.invalidate()
                    } else {
                        Toast.makeText(requireContext(), "Błąd pobierania wykresu (zachowano poprzedni)", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateChart(history: List<Pair<Long, Double>>, label: String) {
        if (_binding == null) return

        val entries = history.mapIndexed { index, pair ->
            Entry(index.toFloat(), pair.second.toFloat())
        }

        val typedValue = android.util.TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
        val primaryColor = typedValue.data

        val dataSet = LineDataSet(entries, label).apply {
            color = primaryColor
            setDrawCircles(false)
            lineWidth = 2.5f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = primaryColor
            fillAlpha = 60
        }

        binding.cryptoChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.textColor = primaryColor

            xAxis.apply {
                textColor = primaryColor
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = object : ValueFormatter() {
                    private val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        if (index >= 0 && index < history.size) {
                            return dateFormat.format(Date(history[index].first))
                        }
                        return ""
                    }
                }
            }

            axisLeft.apply {
                textColor = primaryColor
                setDrawGridLines(true)
            }
            axisRight.isEnabled = false

            invalidate()
        }
    }

    private fun swapUnits() {
        if (fromUnit == null || toUnit == null) return

        val temp = fromUnit
        fromUnit = toUnit
        toUnit = temp

        val tempText = binding.autoCompleteFrom.text.toString()
        binding.autoCompleteFrom.setText(binding.autoCompleteTo.text.toString(), false)
        binding.autoCompleteTo.setText(tempText, false)

        performConversion()
        loadChartData()
    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        val currentFocusView = view?.findFocus()
        if (currentFocusView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
            currentFocusView.clearFocus()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}