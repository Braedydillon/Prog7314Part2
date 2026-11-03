package com.example.prog7314p2

import RetrofitClient
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.prog7314p2.Models.Category
import com.example.prog7314p2.Models.CreateProductRequest
import com.example.prog7314p2.util.ImageCompressor
import com.example.prog7314p2.util.serverMessage
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.math.BigDecimal

class AdminAddProductFragment : Fragment() {

    private var pickedImage: Uri? = null
    private var categories: List<Category> = emptyList()

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            pickedImage = uri
            showPickedImage()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_admin_add_product, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pickedImage = savedInstanceState?.let { BundleCompat.getParcelable(it, KEY_IMAGE, Uri::class.java) }
        showPickedImage()

        view.findViewById<View>(R.id.cardImage).setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        view.findViewById<Button>(R.id.btnSaveProduct).setOnClickListener { save() }

        loadCategories()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        pickedImage?.let { outState.putParcelable(KEY_IMAGE, it) }
    }

    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                categories = RetrofitClient.instance.getCategories().filter { it.active }
                val labels = listOf(getString(R.string.admin_choose_category)) + categories.map { it.name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                requireView().findViewById<Spinner>(R.id.spinnerCategory).adapter = adapter
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("Admin", "Couldn't load categories", e)
                Toast.makeText(requireContext(), R.string.admin_categories_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPickedImage() {
        val view = view ?: return
        val imageView = view.findViewById<ImageView>(R.id.ivProductImage)
        val placeholder = view.findViewById<View>(R.id.layoutImagePlaceholder)
        val uri = pickedImage

        if (uri == null) {
            imageView.setImageDrawable(null)
            placeholder.visibility = View.VISIBLE
        } else {
            imageView.load(uri)
            placeholder.visibility = View.GONE
            view.findViewById<View>(R.id.tvImageError).visibility = View.GONE
        }
    }

    private fun readForm(view: View): CreateProductRequest? {
        val tilName = view.findViewById<TextInputLayout>(R.id.tilName)
        val tilPrice = view.findViewById<TextInputLayout>(R.id.tilPrice)
        val tilSalePrice = view.findViewById<TextInputLayout>(R.id.tilSalePrice)
        val tilStock = view.findViewById<TextInputLayout>(R.id.tilStock)
        val tvImageError = view.findViewById<TextView>(R.id.tvImageError)
        val tvCategoryError = view.findViewById<TextView>(R.id.tvCategoryError)

        listOf(tilName, tilPrice, tilSalePrice, tilStock).forEach { it.error = null }
        var ok = true

        val name = textOf(view, R.id.etName)
        if (name.isEmpty()) {
            tilName.error = getString(R.string.admin_error_name); ok = false
        }

        val price = textOf(view, R.id.etPrice).toMoneyOrNull()
        if (price == null || price <= BigDecimal.ZERO) {
            tilPrice.error = getString(R.string.admin_error_price); ok = false
        }

        val saleText = textOf(view, R.id.etSalePrice)
        val salePrice = saleText.toMoneyOrNull()
        if (saleText.isNotEmpty() &&
            (salePrice == null || salePrice <= BigDecimal.ZERO || (price != null && salePrice >= price))
        ) {
            tilSalePrice.error = getString(R.string.admin_error_sale_price); ok = false
        }

        val stock = textOf(view, R.id.etStock).toIntOrNull()
        if (stock == null || stock < 0) {
            tilStock.error = getString(R.string.admin_error_stock); ok = false
        }

        val category = categories.getOrNull(view.findViewById<Spinner>(R.id.spinnerCategory).selectedItemPosition - 1)
        tvCategoryError.visibility = if (category == null) View.VISIBLE else View.GONE
        if (category == null) ok = false

        tvImageError.visibility = if (pickedImage == null) View.VISIBLE else View.GONE
        if (pickedImage == null) ok = false

        if (!ok) return null

        return CreateProductRequest(
            name = name,
            description = textOf(view, R.id.etDescription).ifEmpty { null },
            price = price!!,
            discountPrice = salePrice,
            stock = stock!!,
            brand = textOf(view, R.id.etBrand).ifEmpty { null },
            categoryId = category!!.id
        )
    }

    private fun textOf(view: View, id: Int): String =
        view.findViewById<TextView>(id).text.toString().trim()

    private fun String.toMoneyOrNull(): BigDecimal? =
        replace(',', '.').toBigDecimalOrNull()?.takeIf { it.stripTrailingZeros().scale() <= 2 }

    private fun save() {
        val view = view ?: return
        val request = readForm(view) ?: return
        val imageUri = pickedImage ?: return

        setSaving(view, true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val jpeg = ImageCompressor.toJpeg(requireContext(), imageUri)

                val productPart = Gson().toJson(request).toRequestBody("application/json".toMediaType())
                val imagePart = MultipartBody.Part.createFormData(
                    "image", "product.jpg", jpeg.toRequestBody("image/jpeg".toMediaType())
                )

                RetrofitClient.instance.createProduct(productPart, imagePart)

                Toast.makeText(requireContext(), R.string.admin_product_added, Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("Admin", "Create product failed", e)
                val message = e.serverMessage() ?: getString(R.string.admin_save_failed)
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                setSaving(view, false)
            }
        }
    }

    private fun setSaving(view: View, saving: Boolean) {
        view.findViewById<Button>(R.id.btnSaveProduct).isEnabled = !saving
        view.findViewById<View>(R.id.cardImage).isEnabled = !saving
        view.findViewById<View>(R.id.progressSave).visibility = if (saving) View.VISIBLE else View.GONE
    }

    companion object {
        private const val KEY_IMAGE = "picked_image"
    }
}
