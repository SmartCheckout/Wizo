package com.wizo.smartcheckout.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;


import com.wizo.smartcheckout.R;
import com.wizo.smartcheckout.activity.MainActivity;
import com.wizo.smartcheckout.model.CartItem;
import com.wizo.smartcheckout.model.Product;
import com.wizo.smartcheckout.model.Weight;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.wizo.smartcheckout.constant.constants.SPINNER_MAX_VALUE;

/**
 * Created by yeshwanth on 4/4/2017.
 */

public class CartListViewAdapter extends BaseAdapter {

    // View lookup cache
    private static class ViewHolder {
        ImageView productImg;
        TextView productTitle;
        TextView productDesc;
        TextView sellingPrice;
        Spinner quantity;
        ArrayAdapter<Integer> quantityAdapter;
    }

    private Context context;
    private List<CartItem> cartItemList;
    private HashMap<String, CartItem> itemTracker;
    private ArrayList<Integer> quantList;
    private Double totalAmount = 0.0;
    private Double totalSavings = 0.0;
    private Double totalWeight = 0.0;

    //Creates an adapter from an already defined list
    public CartListViewAdapter(Context context, List<CartItem> cartItemList) {
        super();
        this.context = context;
        this.quantList = new ArrayList<Integer>();
        this.cartItemList = new ArrayList<CartItem>();
        this.itemTracker = new HashMap<>();

        for (int i=1; i <= SPINNER_MAX_VALUE; i++) {
            this.quantList.add(i);
        }
        for (CartItem item : cartItemList) {
            addItem(item);
        }
    }

    //Creates an adapter with a new list
    public CartListViewAdapter(Context context) {
        this.context = context;
        this.cartItemList = new ArrayList<CartItem>();
        this.itemTracker = new HashMap<>();
        this.quantList = new ArrayList<Integer>();
        for (int i=1; i <= SPINNER_MAX_VALUE; i++) {
            this.quantList.add(i);
        }
    }

    public CartItem findItemInCart(CartItem cartItem) {
        return itemTracker.get(cartItem.getProduct().getBarcode());
    }

    public void addItem(CartItem cartItem) {
        CartItem iteminCart = findItemInCart(cartItem);
        Product currentProduct = cartItem.getProduct();
        System.out.println(currentProduct.getBarcode());

        if (iteminCart == null) {
            // Item not in cart
            cartItemList.add(0,cartItem);
            itemTracker.put(cartItem.getProduct().getBarcode(), cartItem);

            // Updating the total amount and total savings of the contents of the cart
            totalAmount += cartItem.getQuantity() * currentProduct.getSellingPrice();
            totalSavings += cartItem.getQuantity()* currentProduct.getSavings();

            if(currentProduct.getWeight().getUnit().equals(Weight.Unit.GM))
                totalWeight += cartItem.getQuantity() * currentProduct.getWeight().getvalue() ;
            else
                totalWeight += cartItem.getQuantity() * currentProduct.getWeight().getvalue()*1000;


        } else {
            // Item alreay in cart
            // Updating the total amount and total savings of the contents of the cart based on the increase in quantity
            int currentQty = iteminCart.getQuantity();
            int qtyIncrease = cartItem.getQuantity();
            iteminCart.setQuantity(currentQty + qtyIncrease);
            totalAmount += qtyIncrease * currentProduct.getSellingPrice();
            totalSavings += qtyIncrease* currentProduct.getSavings();

            if(currentProduct.getWeight().getUnit().equals(Weight.Unit.GM))
                totalWeight += qtyIncrease * currentProduct.getWeight().getvalue();
            else
                totalWeight += qtyIncrease * currentProduct.getWeight().getvalue()*1000;

        }

        notifyDataSetChanged();
    }

    public Double getTotalAmount(){return totalAmount;}
    public Double getTotalSavings(){return totalSavings;}
    public Double getTotalWeight(){ return totalWeight; }

    @Override
    public int getCount() {
        return cartItemList.size();
    }

    @Override
    public Object getItem(int i) {
        if (i < cartItemList.size())
            return cartItemList.get(i);
        else
            return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;

        System.out.println("-----In getView----");

        //Get the cart item for this position
        final CartItem item = (CartItem) getItem(position);
        System.out.println("Item ----> "+item.getProduct().getTitle());

        if (item != null) {

            if (view == null) {
                // If there's no view to re-use, inflate a brand new view for row

                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.cart_item, parent, false);
                viewHolder = new ViewHolder();

                viewHolder.productImg = (ImageView) view.findViewById(R.id.productImg);
                viewHolder.productTitle = (TextView) view.findViewById(R.id.productTitle);
                viewHolder.productDesc = (TextView) view.findViewById(R.id.productDesc);

                //Set attributes for quantity here so that they are also cached by viewHolder
                viewHolder.quantity = (Spinner) view.findViewById(R.id.quantity);
                viewHolder.quantityAdapter = new ArrayAdapter<Integer>(this.context, R.layout.spinner_item, quantList);
                // Specify the layout to use when the list of choices appears
                viewHolder.quantityAdapter.setDropDownViewResource(R.layout.spinner_drop_down);
                viewHolder.quantity.setAdapter(viewHolder.quantityAdapter);
                viewHolder.sellingPrice = (TextView) view.findViewById(R.id.sellingPrice);
                // Cache the viewHolder object inside the fresh view
                view.setTag(viewHolder);
            } else {
                // View is being recycled, retrieve the viewHolder object from tag
                viewHolder = (ViewHolder) view.getTag();
            }


            DecimalFormat df = new DecimalFormat("#.00");

            //Populate product details ** Need to set image...discuss with Yesh
            viewHolder.productTitle.setText(item.getProduct().getTitle());
            viewHolder.productDesc.setText(item.getProduct().getDescription());
            viewHolder.sellingPrice.setText(df.format(item.getQuantity() * item.getProduct().getSellingPrice()));
            try {
                int imageId = context.getResources().getIdentifier(item.getProduct().getCategory().toLowerCase() + "_icon", "drawable", context.getPackageName());
                Log.e("swetha",imageId+" ");
                if(imageId == 0)
                    viewHolder.productImg.setImageResource(R.drawable.default_icon);
                else
                    viewHolder.productImg.setImageResource(imageId);
            }
            catch(Exception e)
            {
                viewHolder.productImg.setImageResource(R.drawable.default_icon);
            }
            //Add listener and update quantity
            viewHolder.quantity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> spinnerParent, View spinnerView, int spinnerPosition, long id) {
                    Product currentProduct =  item.getProduct();
                    int newQuantity = (Integer)spinnerParent.getItemAtPosition(spinnerPosition);
                    int qtyDifference = newQuantity - item.getQuantity();
                    totalAmount += qtyDifference* currentProduct.getSellingPrice();
                    totalSavings += qtyDifference* currentProduct.getSavings();

                    if(currentProduct.getWeight().getUnit().equals(Weight.Unit.GM))
                        totalWeight += qtyDifference * currentProduct.getWeight().getvalue();
                    else
                        totalWeight += qtyDifference * currentProduct.getWeight().getvalue()*1000;

                    item.setQuantity(newQuantity);
                    notifyDataSetChanged();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            //Set the item quantity in spinner
            viewHolder.quantity.setSelection(viewHolder.quantityAdapter.getPosition(item.getQuantity()));

            // Check with Yesh whether savings need to be shown at item level
            /*if(savings >0){
                ((TextView)view.findViewById(R.id.itemSavings)).setText("Saved : $" + df.format(savings));
                ((TextView)view.findViewById(R.id.itemSavings)).setVisibility(View.VISIBLE);
            }else{
                ((TextView)view.findViewById(R.id.itemSavings)).setVisibility(View.GONE);
            }*/


        }


        return view;
    }

    public List<CartItem> getCartItemList() {
        return cartItemList;
    }

    //Removes the specified item
    public void remove(CartItem cartItem) {
      // Updating the total amount and savings based on the item being removed
        Product currentProduct = cartItem.getProduct();
        totalSavings -= cartItem.getQuantity()*currentProduct.getSavings();
        totalAmount -= cartItem.getQuantity()*currentProduct.getSellingPrice();

        if(currentProduct.getWeight().getUnit().equals(Weight.Unit.GM))
            totalWeight -= cartItem.getQuantity() * currentProduct.getWeight().getvalue();
        else
            totalWeight -= cartItem.getQuantity() * currentProduct.getWeight().getvalue()*1000;

        cartItemList.remove(cartItem);
        itemTracker.remove(cartItem.getProduct().getBarcode());
    }


}
