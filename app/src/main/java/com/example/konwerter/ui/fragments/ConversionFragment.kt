package com.example.konwerter.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ConversionFragment : Fragment() {

    private var _binding: FragmentConversionBinding? = null
    private val binding get() = _binding!!

    private lateinit var category: Category
    private var units: List<Unit> = emptyList()
    private var fromUnit: Unit? = null
    private var toUnit: Unit? = null
    private var retryCount = 0

    private var decimalFormat = java.text.DecimalFormat("#.########")

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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
    }

    private fun updateDecimalFormat() {
        val decimalPlaces = PreferencesManager.getDecimalPlaces(requireContext())
        val pattern = "#." + "#".repeat(decimalPlaces)
        decimalFormat = java.text.DecimalFormat(pattern)
    }

    private fun loadCryptoUnits() {
        showLoading(true)
        coroutineScope.launch {
            try {
                units = CryptoRepository.getCryptoUnits()
                if (_binding != null) {
                    showError(false)
                    setupUI()
                }
            } catch (e: Exception) {
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
            showError(true)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        // You can add a ProgressBar here if you want
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
    }

    private fun setupDropdowns() {
        // ... (as before)
    }

    private fun setupInputListeners() {
        // ... (as before)
    }

    private fun performConversion() {
       // ... (as before)
    }

    private fun loadChartData() {
        // ... (as before)
    }

    private fun updateChart(history: List<Pair<Long, Double>>, label: String) {
       // ... (as before)
    }

    private fun swapUnits() {
        // ... (as before)
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
                arguments = Bundle().apply { putString(ARG_CATEGORY, category.name) }
            }
        }
    }
}