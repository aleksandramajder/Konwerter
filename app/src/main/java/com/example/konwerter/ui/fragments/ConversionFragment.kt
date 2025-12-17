package com.example.konwerter.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.konwerter.R
import com.example.konwerter.data.Category
import com.example.konwerter.data.Converter
import com.example.konwerter.data.CryptoRepository
import com.example.konwerter.data.Unit
import com.example.konwerter.data.UnitRepository
import com.example.konwerter.databinding.FragmentConversionBinding
import com.example.konwerter.utils.PreferencesManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversionFragment : Fragment() {

    private var _binding: FragmentConversionBinding? = null
    private val binding get() = _binding!!

    private lateinit var category: Category
    private var units: List<Unit> = emptyList()
    private var fromUnit: Unit? = null
    private var toUnit: Unit? = null
    private var retryCount = 0

    private var decimalFormat = DecimalFormat("#.########")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getString(ARG_CATEGORY)?.let { Category.valueOf(it) } ?: Category.LENGTH
    }

    override fun onResume() {
        super.onResume()
        if (category == Category.CRYPTO) {
            loadCryptoUnits()
        } else {
            units = UnitRepository.getUnitsForCategory(category)
            setupUI()
        }
    }

    private fun setupUI() {
        if (_binding == null) return
        updateDecimalFormat()
        setupDropdowns()
        setupInputListeners()
        setupCryptoUI()
    }

    private fun updateDecimalFormat() {
        val decimalPlaces = PreferencesManager.getDecimalPlaces(requireContext())
        val pattern = "#." + "#".repeat(decimalPlaces)
        decimalFormat = DecimalFormat(pattern)
    }

    private fun loadCryptoUnits() {
        showLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                units = CryptoRepository.getCryptoUnits()
                if (_binding != null) {
                    showError(false)
                    setupUI()
                    retryCount = 0
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (_binding != null) {
                    handleNetworkError()
                }
            } finally {
                if (_binding != null) {
                    showLoading(false)
                }
            }
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
        }

        binding.retryButton.setOnClickListener {
            retryCount = 0
            loadCryptoUnits()
        }

        binding.buttonSwap.setOnClickListener {
            swapUnits()
        }

        binding.buttonRefresh?.setOnClickListener {
            if (category == Category.CRYPTO) {
                CryptoRepository.clearCache()
                loadCryptoUnits()
            }
        }
    }

    private fun setupDropdowns() {
        if (units.isEmpty()) return

        val unitNames = units.map { "${it.name} (${it.symbol})" }

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, unitNames)
        binding.autoCompleteFrom.setAdapter(adapter)
        binding.autoCompleteTo.setAdapter(adapter)

        if (units.isNotEmpty()) {
            if (fromUnit == null) {
                binding.autoCompleteFrom.setText(unitNames[0], false)
                fromUnit = units[0]
            }
            if (toUnit == null && units.size > 1) {
                binding.autoCompleteTo.setText(unitNames[1], false)
                toUnit = units[1]
            }
            
            if (category == Category.CRYPTO) {
                loadChartData()
            }
        }

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
    }

    private fun setupInputListeners() {
        binding.editTextFrom.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                performConversion()
            }
        })
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
        if (category != Category.CRYPTO || fromUnit == null || toUnit == null) return

        val (crypto, fiat) = when {
            CryptoRepository.isCrypto(fromUnit!!) -> fromUnit!! to toUnit!!
            CryptoRepository.isCrypto(toUnit!!) -> toUnit!! to fromUnit!!
            else -> {
                binding.cryptoChart.clear()
                binding.cryptoChart.setNoDataText("Wykres dostępny tylko dla par z kryptowalutą")
                binding.cryptoChart.invalidate()
                return
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val history = CryptoRepository.getHistoricalData(crypto, fiat, "7")

                if (history.isNotEmpty() && _binding != null) {
                    updateChart(history, "${crypto.symbol}/${fiat.symbol}")
                } else if (_binding != null) {
                    binding.cryptoChart.clear()
                    binding.cryptoChart.setNoDataText("Brak danych historycznych dla tej pary.")
                    binding.cryptoChart.invalidate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (_binding != null) {
                    binding.cryptoChart.clear()
                    binding.cryptoChart.setNoDataText("Błąd ładowania wykresu.")
                    binding.cryptoChart.invalidate()
                }
            }
        }
    }

    private fun updateChart(history: List<Pair<Long, Double>>, label: String) {
        if (_binding == null) return

        val entries = history.mapIndexed { index, pair ->
            Entry(index.toFloat(), pair.second.toFloat())
        }

        val dataSet = LineDataSet(entries, label).apply {
            color = ContextCompat.getColor(requireContext(), R.color.blue_primary)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.blue_primary))
            lineWidth = 2f
            circleRadius = 3f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.cryptoChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true

            xAxis.apply {
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

            axisLeft.setDrawGridLines(true)
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