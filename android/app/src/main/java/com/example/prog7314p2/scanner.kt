package com.example.prog7314p2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.prog7314p2.Models.Product
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class scanner : Fragment() {

    private lateinit var previewView: PreviewView
    private var isScanningActive = true

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(context, "Camera permission required for Barcode Scanner", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previewView = view.findViewById(R.id.previewView)
        val etBarcodeManual = view.findViewById<EditText>(R.id.etBarcodeManual)
        val btnSearchBarcode = view.findViewById<Button>(R.id.btnSearchBarcode)

        // Manual Barcode Match Button (For testing on emulators)
        btnSearchBarcode.setOnClickListener {
            val query = etBarcodeManual.text.toString().trim()
            if (query.isNotEmpty()) {
                searchProductAndNavigate(query)
            } else {
                Toast.makeText(context, "Enter a barcode or product ID", Toast.LENGTH_SHORT).show()
            }
        }

        // Check & Request Camera Permission
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        val currentContext = context ?: return
        if (ContextCompat.checkSelfPermission(currentContext, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val currentContext = context ?: return
        val cameraProviderFuture = ProcessCameraProvider.getInstance(currentContext)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val barcodeScanner = BarcodeScanning.getClient()
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    @ExperimentalGetImage
                    val mediaImage = imageProxy.image
                    if (mediaImage != null && isScanningActive) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    val rawValue = barcode.rawValue
                                    if (!rawValue.isNullOrEmpty() && isScanningActive) {
                                        isScanningActive = false // Pause scanning to prevent duplicates
                                        
                                        requireActivity().runOnUiThread {
                                            Toast.makeText(context, "Scanned Barcode: $rawValue", Toast.LENGTH_SHORT).show()
                                            searchProductAndNavigate(rawValue)
                                        }
                                        break
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(currentContext))
    }

    private fun searchProductAndNavigate(barcodeQuery: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Query Zach's API to match the barcode or product ID to an item in the Unify Database!
                val productResponse = RetrofitClient.instance.getProducts(search = barcodeQuery)

                if (productResponse.products.isNotEmpty()) {
                    val matchedProduct = productResponse.products[0]

                    // Open ProductDetailsFragment with the matched product!
                    val detailsFragment = ProductDetailsFragment()
                    val bundle = Bundle().apply {
                        putSerializable("PRODUCT_DATA", matchedProduct)
                    }
                    detailsFragment.arguments = bundle

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, detailsFragment)
                        .addToBackStack(null)
                        .commit()
                } else {
                    Toast.makeText(context, "No matching product found for: $barcodeQuery", Toast.LENGTH_SHORT).show()
                    isScanningActive = true // Re-enable camera scanning
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error matching barcode: ${e.message}", Toast.LENGTH_SHORT).show()
                isScanningActive = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isScanningActive = true
    }
}