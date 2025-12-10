package com.example.konwerter.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.konwerter.R
import com.example.konwerter.data.Category
import com.example.konwerter.data.Converter
import com.example.konwerter.data.CryptoRepository
import com.example.konwerter.data.Unit
import com.example.konwerter.data.UnitRepository
import com.example.konwerter.databinding.FragmentConversionBinding
import com.example.konwerter.utils.PreferencesManager
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.DecimalFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
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

    private var decimalFormat = DecimalFormat("#.########")

    private var isLoadingCrypto = false
    private val coroutineScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.Dispatchers.Main + kotlinx.coroutines.SupervisorJob()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = Category.valueOf(arguments?.getString(ARG_CATEGORY) ?: Category.LENGTH.name)

        if (category == Category.CRYPTO) {
            loadCryptoUnits()
        } else {
            units = UnitRepository.getUnitsForCategory(category)
        }
    }

    override fun onResume() {
        super.onResume()
        updateDecimalFormat()
        performConversion()
    }

    private fun updateDecimalFormat() {
        val decimalPlaces = PreferencesManager.getDecimalPlaces(requireContext())
        val pattern = "#." + "#".repeat(decimalPlaces)
        decimalFormat = DecimalFormat(pattern)
    }

    private fun loadCryptoUnits() {
        isLoadingCrypto = true
        coroutineScope.launch {
            try {
                units = CryptoRepository.getCryptoUnits()
                if (_binding != null) {
                    setupDropdowns()
                }
            } catch (e: Exception) {
                if (context != null) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Błąd pobierania danych kryptowalut",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                units = CryptoRepository.getCryptoUnits()
                if (_binding != null) {
                    setupDropdowns()
                }
            } finally {
                isLoadingCrypto = false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConversionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateDecimalFormat()

        // Configure chart initially
        if (category == Category.CRYPTO) {
            binding.cryptoChart.apply {
                setNoDataText("Wykres dostępny tylko dla kryptowalut (BTC, ETH...)")
                setNoDataTextColor(requireContext().getColor(R.color.black))
                description.isEnabled = false
            }
        }

        if (category != Category.CRYPTO) {
            setupDropdowns()
            setupInputListeners()
        } else if (!isLoadingCrypto && units.isNotEmpty()) {
            setupDropdowns()
            setupInputListeners()
        } else {
            binding.textViewResult.text = "Ładowanie..."
            setupInputListeners()
        }
    }

    private fun setupDropdowns() {
        val unitNames = units.map { "${it.name} (${it.symbol})" }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, unitNames)
        
        binding.autoCompleteFrom.setAdapter(adapter)
        binding.autoCompleteTo.setAdapter(adapter)

        if (units.size >= 2) {
            binding.autoCompleteFrom.setText(adapter.getItem(0).toString(), false)
            fromUnit = units[0]
            
            binding.autoCompleteTo.setText(adapter.getItem(1).toString(), false)
            toUnit = units[1]
        } else if (units.isNotEmpty()) {
             binding.autoCompleteFrom.setText(adapter.getItem(0).toString(), false)
             fromUnit = units[0]
             
             binding.autoCompleteTo.setText(adapter.getItem(0).toString(), false)
             toUnit = units[0]
        }

        binding.autoCompleteFrom.setOnItemClickListener { _, _, position, _ ->
            fromUnit = units[position]
            performConversion()
            loadChartData()
        }

        binding.autoCompleteTo.setOnItemClickListener { _, _, position, _ ->
            toUnit = units[position]
            performConversion()
            loadChartData()
        }

        if (category == Category.CRYPTO && fromUnit != null) {
            loadChartData()
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

        binding.buttonSwap.setOnClickListener {
            swapUnits()
        }
        if (category == Category.CRYPTO) {
            binding.buttonRefresh.visibility = View.VISIBLE
            binding.textViewHint.text = "Kursy aktualizowane co minutę"
            binding.cryptoChart.visibility = View.VISIBLE

            binding.buttonRefresh.setOnClickListener {
                refreshCryptoPrices()
            }
        }
    }

    private fun refreshCryptoPrices() {
        binding.buttonRefresh.isEnabled = false
        binding.buttonRefresh.text = "Odświeżanie..."

        coroutineScope.launch {
            try {
                units = CryptoRepository.getCryptoUnits()
                setupDropdowns()
                performConversion()
                loadChartData()

                if (context != null) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Kursy zaktualizowane",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                if (context != null) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Błąd aktualizacji",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                binding.buttonRefresh.isEnabled = true
                binding.buttonRefresh.text = "Odśwież kursy"
            }
        }
    }

    private fun loadChartData() {
        if (category != Category.CRYPTO || fromUnit == null || toUnit == null) return

        coroutineScope.launch {
            val isFromCrypto = CryptoRepository.isCrypto(fromUnit!!)
            val isToCrypto = CryptoRepository.isCrypto(toUnit!!)
            
            var history: List<Pair<Long, Double>> = emptyList()
            var label = ""

            if (isFromCrypto) {
                // Case 1: From Crypto (e.g. BTC) -> To Fiat/Crypto (e.g. USD, PLN, ETH)
                // API supports vs_currency for some fiats (usd, eur, pln). 
                // If 'toUnit' is one of supported fiats, use it. Otherwise use USD.
                // If 'toUnit' is also crypto (BTC->ETH), coingecko free API market_chart doesn't support vs_currency=eth directly easily?
                // Actually Coingecko supports vs_currency=eth.
                // Let's try to pass toUnit symbol as vs_currency.
                
                history = CryptoRepository.getHistoricalData(fromUnit!!, toUnit!!, "14")
                label = "Kurs ${fromUnit?.symbol} w ${toUnit?.symbol}"
            } else if (isToCrypto) {
                // Case 2: From Fiat (e.g. USD) -> To Crypto (e.g. BTC)
                // We want to show chart of BTC in USD, but inverted? 
                // Or just show chart of ToUnit (BTC) in FromUnit (USD).
                // This shows how much 1 BTC costs in USD over time.
                // Which gives user idea of trend.
                // Usually users want to see the crypto trend.
                
                history = CryptoRepository.getHistoricalData(toUnit!!, fromUnit!!, "14")
                label = "Kurs ${toUnit?.symbol} w ${fromUnit?.symbol}"
            }

            if (history.isNotEmpty()) {
                updateChart(history, label)
            } else {
                if (_binding != null) {
                    binding.cryptoChart.data = null
                    binding.cryptoChart.setNoDataText("Wykres dostępny dla par z kryptowalutą")
                    binding.cryptoChart.invalidate()
                }
            }
        }
    }

    private fun updateChart(history: List<Pair<Long, Double>>, label: String) {
        if (_binding == null) return

        val entries = history.map { (timestamp, price) ->
            Entry(timestamp.toFloat(), price.toFloat())
        }

        // Pobierz kolor motywu
        val themeColor = try {
            val typedValue = android.util.TypedValue()
            requireContext().theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
            typedValue.data
        } catch (e: Exception) {
            requireContext().getColor(R.color.blue_primary)
        }

        val dataSet = LineDataSet(entries, label).apply {
            color = themeColor
            valueTextColor = requireContext().getColor(R.color.black)
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = themeColor
            fillAlpha = 50
        }

        val lineData = LineData(dataSet)
        binding.cryptoChart.data = lineData

        binding.cryptoChart.apply {
            description.isEnabled = false
            legend.isEnabled = true // Show legend to see label
            axisRight.isEnabled = false
            xAxis.setDrawLabels(false)
            animateX(1000)
            invalidate()
        }
    }

    private fun performConversion() {
        val inputText = binding.editTextFrom.text.toString()

        if (inputText.isEmpty() || fromUnit == null || toUnit == null) {
            binding.textViewResult.text = "0"
            return
        }

        try {
            val inputValue = inputText.toDouble()
            val result = Converter.convert(inputValue, fromUnit!!, toUnit!!)
            binding.textViewResult.text = decimalFormat.format(result)
        } catch (_: Exception) {
            binding.textViewResult.text = "Błąd"
        }
    }

    private fun swapUnits() {
        val tempUnit = fromUnit
        fromUnit = toUnit
        toUnit = tempUnit
        
        val fromText = binding.autoCompleteFrom.text.toString()
        val toText = binding.autoCompleteTo.text.toString()
        
        binding.autoCompleteFrom.setText(toText, false)
        binding.autoCompleteTo.setText(fromText, false)
        
        performConversion()
        loadChartData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: Category): ConversionFragment {
            return ConversionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY, category.name)
                }
            }
        }
    }
}