package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.CartDAO;
import com.csye6220.huskyamazon.dao.ProductDAO;
import com.csye6220.huskyamazon.entity.Cart;
import com.csye6220.huskyamazon.entity.CartItem;
import com.csye6220.huskyamazon.entity.Product;
import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

/**
 * cartserviceimplementation class
 * <p>
 * responsible forHandlecart相关的Corebusiness logic，包括cartQuery、productAdd/Delete/Modified、
 * total价automaticcalculate等功能
 * </p>
 *
 * @author HuskyAmazon Team
 * @version 1.0
 */
@Service
public class CartServiceImpl implements CartService {

    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    @Autowired
    public CartServiceImpl(CartDAO cartDAO, ProductDAO productDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    /**
     * Getuser的cart
     * <p>
     * Corebusiness logic：
     * 1. Ifuser没有cart，automaticCreate一个空cart
     * 2. 强制Initializecartproductcollection（解决Hibernate懒load问题）
     * 3. 每次Query时重新calculatetotal价（ensure数据准确性）
     * </p>
     *
     * @param user Query的user
     * @return user的cartobject（includeproductcolumntable和total价）
     * @apiNote 即使database中的total价不准确，也会automatic重新calculate并修正
     */
    @Override
    @Transactional
    public Cart getCartByUser(User user) {
        Cart cart = cartDAO.findByUser(user);
        
        // Ifuser还没有cart，automaticCreate一个
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setItems(new ArrayList<>());
            cart.setTotalAmount(0.0);
            cartDAO.save(cart);
        }

        // 强制Initializeitemscollection（触发Hibernateload，avoid懒loadexception）
        if (cart.getItems() != null) {
            cart.getItems().size();
        }

        // Key修复：每次Querycart时，强制重新calculatetotal价
        // 这样即使database中的total_amount不准确，也会automatic修正
        recalculateTotal(cart);

        return cart;
    }

    /**
     * Addproduct到cart
     * <p>
     * Corebusiness logic：
     * 1. Validateproduct是否存在
     * 2. Checkcart中是否已有该product
     * 3. Ifalready exists，增加quantity；Otherwise新建cart项
     * 4. 重新calculatecarttotal价
     * </p>
     *
     * @param user      currentuser
     * @param productId 要Add的productID
     * @param quantity  Add的quantity
     * @throws RuntimeException productdoesn't exist时throw exception
     */
    @Override
    @Transactional
    public void addItemToCart(User user, Long productId, int quantity) {
        Cart cart = getCartByUser(user);
        Product product = productDAO.findById(productId);

        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        // Checkcart中是否已经有这个product
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // productalready exists，增加quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // productdoesn't exist，Create新的cart项
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }

        // 重新calculatetotal价
        recalculateTotal(cart);
        cartDAO.update(cart);
    }

    /**
     * 从cart中Removeproduct
     *
     * @param user      currentuser
     * @param productId 要Remove的productID
     */
    @Override
    @Transactional
    public void removeItemFromCart(User user, Long productId) {
        Cart cart = getCartByUser(user);
        
        // 从cartcolumntable中Delete指定product
        boolean removed = cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));

        if (removed) {
            // Deletesuccessful后重新calculatetotal价
            recalculateTotal(cart);
            cartDAO.update(cart);
        }
    }

    /**
     * Updatecart中product的quantity
     * <p>
     * Corebusiness logic：
     * 1. Ifquantity小于等于0，直接Delete该product
     * 2. OtherwiseUpdateproductquantity并重新calculatetotal价
     * </p>
     *
     * @param user      currentuser
     * @param productId 要Update的productID
     * @param quantity  新的quantity
     */
    @Override
    @Transactional
    public void updateItemQuantity(User user, Long productId, int quantity) {
        // quantity小于等于0时，直接Delete该product
        if (quantity <= 0) {
            removeItemFromCart(user, productId);
            return;
        }

        Cart cart = getCartByUser(user);
        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            item.setQuantity(quantity);
            // 重新calculatetotal价
            recalculateTotal(cart);
            cartDAO.update(cart);
        }
    }

    /**
     * Clearcart
     * <p>
     * Deletecart中的allproduct，并将total价reset为0
     * </p>
     *
     * @param user currentuser
     */
    @Override
    @Transactional
    public void clearCart(User user) {
        Cart cart = getCartByUser(user);
        cart.getItems().clear();
        cart.setTotalAmount(0.0);
        cartDAO.update(cart);
    }

    /**
     * 重新calculatecarttotalamount（private辅助method）
     * <p>
     * 遍历cart中的allproduct，calculate unit price × quantity 的total和
     * 并Updatecart的totalAmountfield
     * </p>
     *
     * @param cart 要calculate的cartobject
     */
    private void recalculateTotal(Cart cart) {
        double total = 0.0;
        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                total += item.getProduct().getPrice() * item.getQuantity();
            }
        }
        cart.setTotalAmount(total);
    }
}
