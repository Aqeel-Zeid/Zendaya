package com.coconutcoders.zendaya.zendayaBackend.controller;


import com.coconutcoders.zendaya.zendayaBackend.model.*;
import com.coconutcoders.zendaya.zendayaBackend.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class ProductController {
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ImageRepo imageRepo;
    @Autowired
    private ProductCategoryRepo productCategoryRepo;
    @Autowired
    private ShoppingCartRepo shoppingCartRepo;
    @Autowired
    private WishListRepo wishListRepo;

    /**
     * Adds Product to Database
     * POST to http://localhost:8080/addProduct
     *
     * @param payload should contain JSON key-value pairs with key(s): "productName", "price" and "description". Optional key "discount" can be included
     * @return CONFLICT if a product with same name is already in DB, else OK
     */
    @RequestMapping(value = "/addProduct", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity addProduct(@RequestBody Map<String, String> payload) {
        if (!payload.containsKey("productName") || !payload.containsKey("description") || !payload.containsKey("price")) {
            return new ResponseEntity<>("required key(s) not found in JSON Body", HttpStatus.NOT_FOUND);
        }
        final String productName = payload.get("productName");
        final String description = payload.get("description");
        final double price = Double.parseDouble(payload.get("price"));

        Product product = productRepo.findByNameIgnoreCase(productName);
        if (product != null) {
            return new ResponseEntity<>("Product already in database", HttpStatus.CONFLICT);
        }

        product = new Product(productName, description, price);
        if (payload.containsKey("discount"))     //Optional JSON value
        {
            double discount = Double.parseDouble(payload.get("discount"));
            product.setDiscountPercentage(discount);
        }
        productRepo.save(product);
        return new ResponseEntity<>(product.getName() + " Added to Database", HttpStatus.OK);
    }

    /**
     * Adds a Review for a product, if a review is already present it will be updated
     * POST to http://localhost:8080/addReview
     *
     * @param payload should contain JSON key-value pairs with key(s): "productName", "username", "description" and "rating"
     * @return NOT FOUND if product is not in database, else OK
     */
    @RequestMapping(value = "/addReview", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity addReview(@RequestBody Map<String, String> payload) {
        if (!payload.containsKey("productName") || !payload.containsKey("username")
                || !payload.containsKey("description") || !payload.containsKey("rating")) {
            return new ResponseEntity<>("required key(s) not found in JSON Body", HttpStatus.NOT_FOUND);
        }
        final String productName = payload.get("productName");
        final String username = payload.get("username");
        final String description = payload.get("description");
        final String ratingS = payload.get("rating");

        Product product = productRepo.findByNameIgnoreCase(productName);
        if (product == null) {
            return new ResponseEntity<>("Product Not Found in database", HttpStatus.NOT_FOUND);
        }

        double rating = Double.parseDouble(ratingS);
        product.addOrUpdateReview(username, description, rating);

        productRepo.save(product);
        return new ResponseEntity<>(username + "'s review for " + product.getName() + " Added to Database", HttpStatus.OK);
    }

    /**
     * gets reviews made by user
     * POST to http://localhost:8080/getReviews
     *
     * @param payload should contain JSON key-value pairs with key(s): "username"
     * @return NOT FOUND if product is not in database, else OK
     */
    @RequestMapping(value = "/getReviews", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity getReviews(@RequestBody Map<String, String> payload) {
        if (!payload.containsKey("username")) {
            return new ResponseEntity<>("required key(s) not found in JSON Body", HttpStatus.NOT_FOUND);
        }
        final String username = payload.get("username");

        List<Product> productList = productRepo.findAll();
        if (productList == null || productList.isEmpty()) {
            return new ResponseEntity<>("Product Not Found in database", HttpStatus.NOT_FOUND);
        }

        HashMap<String, Object> response = new HashMap<>();
        for (Product product : productList) {
            if (product.getUserReview(username) != null) {
                HashMap<String, Object> temp = new HashMap<>();
                temp.put("productName", product.getName());
                temp.put("review", product.getUserReview(username));
                temp.put("rating", product.getUserRating(username));
                temp.put("timeStamp", product.getUserTimeStamp(username));
                response.put(product.getName(), temp);
            }
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Removes Product from Database
     * POST to http://localhost:8080/removeProduct
     *
     * @param payload should contain JSON key-value pairs with key(s): "productName"
     * @return NOT_FOUND if the product is not found in DB, else OK
     */
    @RequestMapping(value = "/removeProduct", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity removeProduct(@RequestBody Map<String, String> payload) {
        if (!payload.containsKey("productName")) {
            return new ResponseEntity<>("required key(s) not found in JSON Body", HttpStatus.NOT_FOUND);
        }
        final String productName = payload.get("productName");

        Product product = productRepo.findByNameIgnoreCase(productName);
        if (product == null) {
            return new ResponseEntity<>("No such Product in database", HttpStatus.NOT_FOUND);
        }

        productRepo.delete(product);

        //Deleting Product images
        Image image = imageRepo.findByProductName(productName);
        if (image != null) {
            imageRepo.delete(image);
        }

        //Deleting product from categories
        List<ProductCategory> productCategories = productCategoryRepo.findAll();
        if (productCategories != null && !productCategories.isEmpty()) {
            for (ProductCategory productCategory : productCategories) {
                if (productCategory.doesCategoryContainProduct(productName)) {
                    productCategory.removeProductFromCategory(productName);
                    productCategoryRepo.save(productCategory);
                }
            }
        }

        //Deleting product from Shopping carts
        List<ShoppingCart> shoppingCarts = shoppingCartRepo.findAll();
        if (shoppingCarts != null && !shoppingCarts.isEmpty()) {
            for (ShoppingCart shoppingCart : shoppingCarts) {
                if (shoppingCart.isProductAlreadyInCart(productName)) {
                    shoppingCart.removeProduct(productName);
                    shoppingCartRepo.save(shoppingCart);
                }
            }
        }

        //Deleting product from Shopping carts
        List<WishList> wishLists = wishListRepo.findAll();
        if (wishLists != null && !wishLists.isEmpty()) {
            for (WishList wishlist : wishLists) {
                if (wishlist.isProductAlreadyInWishList(productName)) {
                    wishlist.removeProduct(productName);
                    wishListRepo.save(wishlist);
                }
            }
        }

        return new ResponseEntity<>(product.getName() + " Deleted from Database", HttpStatus.OK);
    }

    /**
     * Update existing Product in Database
     * POST to http://localhost:8080/updateProduct
     *
     * @param payload should contain JSON key-value pairs with key(s): "productName", "newProductName", "description", "price", "discount"
     * @return NOT_FOUND if no such Product in DB, else OK
     */
    @RequestMapping(value = "/updateProduct", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity updateProduct(@RequestBody Map<String, String> payload) {
        if (!payload.containsKey("productName") || !payload.containsKey("newProductName") || !payload.containsKey("description")
                || !payload.containsKey("price") || !payload.containsKey("discount")) {
            return new ResponseEntity<>("required key(s) not found in JSON Body", HttpStatus.NOT_FOUND);
        }
        final String productName = payload.get("productName");
        final String newProductName = payload.get("newProductName");
        final String description = payload.get("description");
        final double price = Double.parseDouble(payload.get("price"));
        final int discount = Integer.parseInt(payload.get("discount"));

        Product product = productRepo.findByNameIgnoreCase(productName);
        if (product == null) {
            return new ResponseEntity<>("Product Not Found in database", HttpStatus.NOT_FOUND);
        }

        product.setDescription(description);
        product.setPrice(price);
        product.setDiscountPercentage(discount);
        product.setName(newProductName);

        productRepo.save(product);
        return new ResponseEntity<>(product.getName() + " updated in Database", HttpStatus.OK);
    }

    /**
     * Set a discount for a product
     * POST to http://localhost:8080/setProductDiscount
     *
     * @param payload should contain JSON key-value pairs with key(s): "productName" and "discount".
     * @return NOT_FOUND if no such Product in DB, else OK
     */
    @RequestMapping(value = "/setProductDiscount", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity setProductDiscount(@RequestBody Map<String, String> payload) {
        if (!payload.containsKey("productName") || !payload.containsKey("discount")) {
            return new ResponseEntity<>("required key(s) not found in JSON Body", HttpStatus.NOT_FOUND);
        }
        final String productName = payload.get("productName");
        final double discount = Double.parseDouble(payload.get("discount"));

        Product product = productRepo.findByNameIgnoreCase(productName);
        if (product == null) {
            return new ResponseEntity<>("Product Not Found in database", HttpStatus.NOT_FOUND);
        }

        product.setDiscountPercentage(discount);

        productRepo.save(product);
        return new ResponseEntity<>(product.getName() + " updated in Database", HttpStatus.OK);
    }




    /*////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Searching and Sorting Functions
    */////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * find all product that contains given string in name
     * POST to http://localhost:8080/searchProductsByName
     *
     * @param payload should contain JSON key-value pairs with key(s): "productName".
     * @return A JSON array of the matching products.
     */
    @RequestMapping(value = "/searchProductsByName", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity searchProductsByName(@RequestBody Map<String, String> payload) {
        if (!payload.containsKey("productName")) {
            return new ResponseEntity<>("required key(s) not found in JSON Body", HttpStatus.NOT_FOUND);
        }
        final String productName = payload.get("productName");

        List<Product> products = productRepo.findByNameIgnoreCaseContaining(productName);
        Map<String, Map<String, Object>> response = new HashMap<>();

        for (Product product : products) {
            Map<String, Object> productDetails = new HashMap<>();

            productDetails.put("name", product.getName());

            HashMap<String, Number> priceDetails = new HashMap<>();
            priceDetails.put("originalPrice", product.getPrice());
            priceDetails.put("discountPercentage", product.getDiscountPercentage());
            priceDetails.put("finalPrice", product.getPriceWithDiscount());
            productDetails.put("price", priceDetails);

            productDetails.put("description", product.getDescription());
            productDetails.put("average_rating", product.getAvgRating());

            HashMap<String, HashMap<String, Object>> reviews = new HashMap<>();
            for (Map.Entry<String, HashMap<String, String>> reviewDetails : product.getReviews().entrySet()) {
                HashMap<String, Object> temp = new HashMap<>();
                temp.put("time_stamp", reviewDetails.getValue().get("timeStamp"));
                temp.put("username", reviewDetails.getKey());
                temp.put("review", reviewDetails.getValue().get("review"));
                temp.put("rating", reviewDetails.getValue().get("rating"));
                reviews.put(reviewDetails.getKey(), temp);
            }
            productDetails.put("reviews", reviews);

            response.put(product.getName(), productDetails);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * find all products that have a discount
     * POST to http://localhost:8080/searchProductWithDiscount
     *
     * @return A JSON array of the matching products.
     */
    @RequestMapping(value = "/searchProductWithDiscount", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity searchProductWithDiscount() {
        List<Product> products = productRepo.findByDiscountPercentageGreaterThan(0);

        Map<String, Product> response = new HashMap<>();
        for (Product product : products) {
            response.put(product.getName(), product);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * find all products that gave a rating greater than or equal to a value
     * POST to http://localhost:8080/findProductsWithRatingGreaterThanAndEqual
     *
     * @param payload should contain JSON key-value pairs with key(s): "rating".
     * @return A JSON array of the matching products.
     */
    @RequestMapping(value = "/findProductsWithRatingGreaterThanAndEqual", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity findProductsWithRatingGreaterThanAndEqual(@RequestBody Map<String, String> payload) {
        if (!payload.containsKey("rating")) {
            return new ResponseEntity<>("required key(s) not found in JSON Body", HttpStatus.NOT_FOUND);
        }
        double rating = Double.parseDouble(payload.get("rating"));

        List<Product> products = productRepo.findByAvgRatingGreaterThanEqual(rating);

        Map<String, Product> response = new HashMap<>();
        for (Product product : products) {
            response.put(product.getName(), product);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get a perticular products original price, discount and final price
     * POST to http://localhost:8080/getProductPricingDetails
     *
     * @param payload should contain JSON key-value pairs with key(s): "productName".
     * @return A JSON array with key(s): "originalPrice", "discountPercentage", "finalPrice"
     */
    @RequestMapping(value = "/getProductPricingDetails", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity getProductPricingDetails(@RequestBody Map<String, String> payload) {
        if (!payload.containsKey("productName")) {
            return new ResponseEntity<>("required key(s) not found in JSON Body", HttpStatus.NOT_FOUND);
        }
        final String productName = payload.get("productName");

        Product product = productRepo.findByNameIgnoreCase(productName);

        if (product == null) {
            return new ResponseEntity<>("no such product found", HttpStatus.NOT_FOUND);
        }

        Map<String, Number> response = new HashMap<>();
        response.put("originalPrice", product.getPrice());
        response.put("discountPercentage", product.getDiscountPercentage());
        response.put("finalPrice", product.getPriceWithDiscount());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
