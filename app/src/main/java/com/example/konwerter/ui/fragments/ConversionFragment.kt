package com.example.konwerter.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import java.text.DecimalFormat
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
    private var retryCount = 0
    private var isListenersSetup = false
    private var chartJob: Job? = null

    private var decimalFormat = DecimalFormat("#.########")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getString(ARG_CATEGORY)?.let { Category.valueOf(it) } ?: Category.LENGTH
    }

    override fun onResume() {
        super.onResume()
        if (category == Category.CRYPTO) {
            loadCryptoUnits(isInitialLoad = true)
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
        decimalFormat = DecimalFormat(pattern)
    }

    private fun loadCryptoUnits(isInitialLoad: Boolean = false, onComplete: (() -> Unit)? = null) {
        showLoading(true)
        binding.buttonRefresh?.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                units = CryptoRepository.getCryptoUnits()
                if (_binding != null) {
                    showError(false)
                    retryCount = 0
                    onComplete?.invoke() ?: setupUI()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (_binding != null) {
                    if (isInitialLoad) {
                        handleNetworkError()
                    } else {
                        val message = if (e is java.net.SocketTimeoutException) "Przekroczono limit czasu żądania" else "Nie udało się odświeżyć kursów"
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
        val fromSymbol = fromUnit?.symbol
        val toSymbol = toUnit?.symbol

        loadCryptoUnits {
            fromUnit = units.find { it.symbol == fromSymbol } ?: units.getOrNull(0)
            toUnit = units.find { it.symbol == toSymbol } ?: units.getOrNull(1)

            setupUI()
        }
    }

    private fun handleNetworkError() {
        retryCount++
        if (retryCount > 5) {
            showError(true, "Błąd sieci. Spróbuj ponownie później.")
            binding.retryButton.isEnabled = false
        } else {
            showError(true, "Brak połączenia z internetem.")
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

        binding.retryButton.setOnClickListener {
            binding.retryButton.isEnabled = false
            retryCount = 0
            loadCryptoUnits(isInitialLoad = true)
        }

        binding.buttonSwap.setOnClickListener {
            swapUnits()
        }

        binding.buttonRefresh?.setOnClickListener {
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
            fromUnit = units[position]
            performConversion()
            if (category == Category.CRYPTO) {
                loadChartData()
            }
        }

        binding.autoCompleteTo.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            toUnit = units[position]
            performConversion()
            if (category == Category.CRYPTO) {
                loadChartData()
            }
        }
        isListenersSetup = true
    }

    private fun setupCryptoUI() {
        if (category == Category.CRYPTO) {
            binding.buttonRefresh?.visibility = View.VISIBLE
            binding.cryptoChart.visibility = View.VISIBLE
        } else {
            binding.buttonRefresh?.visibility = View.GONE
            binding.cryptoChart.visibility = View.GONE
        }
    }

    private fun performConversion() {
        val inputText = binding.editTextFrom.text.toString()

        if (fromUnit == null || toUnit == null) {
            binding.textViewResult.text = "0"
            return
        }

        if (category == Category.CRYPTO) {
             binding.buttonSwap.isEnabled = true
             binding.textViewHint.text = "Wpisz wartość aby zobaczyć konwersję"
             binding.cryptoChart.visibility = View.VISIBLE
        }

        if (inputText.isEmpty()) {
            binding.textViewResult.text = "0"
            return
        }

        try {
            val inputValue = inputText.toDouble()
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

        val (crypto, fiat) = when {
            CryptoRepository.isCrypto(fromUnit!!) -> fromUnit!! to toUnit!!
            CryptoRepository.isCrypto(toUnit!!) -> toUnit!! to fromUnit!!
            else -> {
                if (binding.cryptoChart.data == null || binding.cryptoChart.data.entryCount == 0) {
                     binding.cryptoChart.clear()
                     binding.cryptoChart.setNoDataText("Wykres dostępny tylko dla par z kryptowalutą")
                     binding.cryptoChart.invalidate()
                }
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
                        binding.cryptoChart.setNoDataText("Brak danych historycznych dla tej pary.")
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
                        binding.cryptoChart.setNoDataText("Błąd ładowania wykresu.")
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
        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: Category): ConversionFragment {
            return ConversionFragment().apply {
                arguments = Bundle().apply { putString(ARG_CATEGORY, category.name) }
            }
        }
    }
}