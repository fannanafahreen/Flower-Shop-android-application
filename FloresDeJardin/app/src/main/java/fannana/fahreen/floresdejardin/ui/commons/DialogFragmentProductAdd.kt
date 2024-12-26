package fannana.fahreen.floresdejardin.ui.commons

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import fannana.fahreen.floresdejardin.databinding.DialogFragmentItemAddBinding
import fannana.fahreen.floresdejardin.viewmodel.DataStoreViewModel
import fannana.fahreen.floresdejardin.viewmodel.OrderViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class DialogFragmentProductAdd : DialogFragment() {

    private lateinit var binding: DialogFragmentItemAddBinding

    private val orderViewModel: OrderViewModel by viewModels()

    private val dataStoreViewModel: DataStoreViewModel by viewModels()

    private var product : Product? = null

    var orderQty = 0

    var subPrice = 0.0f

    fun init(selectedProduct: Product){
        this.product = selectedProduct
    }

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }*/

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            800
        )
        //dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = DialogFragmentItemAddBinding.inflate(inflater, container, false)
        val root: View = binding.root



        val fName = product?.name
        val fPrice = product?.price ?: 0.0f
        val fImage = product?.image

        binding.apply {
            btnClose.setOnClickListener {
                dialog?.dismiss()
            }

            tvItemName.text = fName
            tvItemPrice.text = "Item Price: $fPrice TK"

            btnPlus.setOnClickListener {
                orderQty++

                tvOrderQty.text = orderQty.toString()

                subPrice = orderQty * fPrice.toFloat()

                tvProductPrice.text = "Total : $subPrice TK"

                btnTotalPrice.text = subPrice.toString()

                if (orderQty == 0) {
                    btnAddToCart.visibility = View.GONE
                } else {
                    btnAddToCart.visibility = View.VISIBLE
                }
            }


            btnMinus.setOnClickListener {
                if (orderQty != 0) {

                    orderQty--

                    tvOrderQty.text = orderQty.toString()

                    subPrice = orderQty * fPrice.toFloat()

                    tvProductPrice.text = "Total : $subPrice TK"

                    btnTotalPrice.text = subPrice.toString()

                    btnAddToCart.visibility = View.VISIBLE

                } else {
                    btnAddToCart.visibility = View.GONE
                }
            }

            btnAddToCart.setOnClickListener {
                product?.let { it1 -> checkDuplicateProduct(it1) }
            }


        }

        return root
    }

    private fun checkDuplicateProduct(product : Product) {
        this.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                orderViewModel.doGetOrderDetails(product.prodCode ?: "").collect {
                    dialog?.dismiss()
                    if (it?.prodID != product.prodID) {
                        saveProductInCart(product)
                    } else {
                        //Toast.makeText(requireContext(),"This item is already added to cart!!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveProductInCart(product : Product){
        val simpleDateFormat = SimpleDateFormat("dd MMM, yyyy")
        val currentDateAndTime: String = simpleDateFormat.format(Date())

        val productCart = ProductCart()
        productCart.prodID     = product.prodID
        productCart.prodCode   = product.prodCode
        productCart.prodName   = product.name
        productCart.prodPrice  = product.price.toFloat()//subPrice
        productCart.prodQty    = orderQty

        //Save the details to room database
        orderViewModel.insertOrderDetails(productCart)

        orderViewModel.response.observe(this){

            Log.d("TAG" , "saveOrderCustomerAsDraft: $it")

            //Success, save key so on next visit user goes to details screen
            dataStoreViewModel.setSavedKey(true)

            //Show toast message
            //Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
            //Toast.makeText(requireContext(), "Record Saved [$it]", Toast.LENGTH_SHORT).show()
        }

    }
}