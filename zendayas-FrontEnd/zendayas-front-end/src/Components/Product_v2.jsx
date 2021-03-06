import React from 'react'
import {Redirect} from "react-router-dom"
import Cookies from "universal-cookie"
import wishList_manager from "../Contexts/Helper_Functions/wishList_management"
import cart_manager from "../Contexts/Helper_Functions/cart_management"

import { Modal,Segment, Reveal, Card, Header, Image, Icon, Transition, Button, Grid , Rating, GridRow, Placeholder, Divider } from "semantic-ui-react"
import ProductDetails from './ProductDetails';


const BACKEND_BASE_URL = process.env.REACT_APP_BACKEND_BASE_URL;



export default function Product_v2(props) {

    const [page, setPage] = React.useState(0)

    const [toLogin,setToLogin] = React.useState(false)

    const [openWishListModal , setOpenWishListModal] = React.useState(false);

    const [openCartModal , setOpenCartModal] = React.useState(false);

    const {
        productName,
        originalPrice,
        THUMBNAIL,
        MAIN_IMAGE ,
        SECONDARY_IMAGE ,
        TERTIARY_IMAGE ,
        finalPrice,
        discount,
        description,
        rating,
        reviews
    } = props;

    const cookies = new Cookies();
    let user_info_cookie = cookies.get("USER");

    const addToCart = async (productName) => 
    {
        if(user_info_cookie !== null && user_info_cookie !== undefined)
        {
            let command_ADD_ITEM_TO_CART = {
                type : "ADD_ITEM_TO_CART",
                payload : {
                    AITC_username :  user_info_cookie.username,
                    AITC_productName : productName
                }
            }
            
            let result_ADD_ITEM_TO_CART = await cart_manager(command_ADD_ITEM_TO_CART)
    
            //console.log(result_REMOVE_WISH_LIST_ITEM)
    
            if(result_ADD_ITEM_TO_CART.status === 200)
            {
                alert("Successfully Added to Cart")
                //fetchWishListItems()
            }
            else
            {
                alert("Fail to Add To Cart")
            }
        }
        
    }


    const AddProductToWishList = async () => 
    {
        if(user_info_cookie !== null && user_info_cookie !== undefined)
        {
            let command_ADD_WISH_LIST_ITEM = {
                type : "ADD_WISH_LIST_ITEM",
                payload : {
                    AWLI_username :  user_info_cookie.username,
                    AWLI_productName : productName
                }
            }
            
            let result_ADD_WISH_LIST_ITEM = await wishList_manager(command_ADD_WISH_LIST_ITEM)
    
            if(result_ADD_WISH_LIST_ITEM.status === 200)
            {
                alert("Successfully Added to Wish List")
            }
            else
            {
                alert("Fail to Add to Wish List")
            }
        }
        

    }


    return (
        <>
                <Card fluid onClick={() => { setPage(1) }}>
                    <Transition visible={page === 0} unmountOnHide animation='scale' duration={350}>
                        <Image src={MAIN_IMAGE} fluid />
                    </Transition>
                    <Transition visible={page === 1} unmountOnHide animation='scale' duration={350}>
                        <Image src={SECONDARY_IMAGE} fluid />
                    </Transition>
                    <Transition visible={page === 2} unmountOnHide animation='scale' duration={350}>
                        <Image src={TERTIARY_IMAGE} fluid />
                    </Transition>
                    
                    <Card.Content>
                        <Card.Header>{productName}</Card.Header>
                        <Card.Meta>
                            <span className='date'>{description}</span>
                            <br/>
                            <Rating icon='heart' defaultRating={rating} maxRating={5} />
                            <Header color = "grey" as = "h4" textAlign="center"><strike>{`LKR ${originalPrice}`}</strike></Header>

                        </Card.Meta>
                        <>
                            <Grid centered>
                               <Grid.Row centered> 
                                    <Grid.Column width = "16" centered>
                                       
                                   </Grid.Column>
                                   <Grid.Column width = "16" centered>
                                        <Header as = "h3" textAlign="center">{`LKR ${finalPrice}`}</Header>
                                   </Grid.Column>
                               </Grid.Row>
                               <Grid.Row>
                               <Grid.Column width={8} textAlign = "center">
                                            <Modal
                                                open = {openCartModal} 
                                                trigger={
                                                                <Button 
                                                                    fluid color = "yellow" centered
                                                                    name = {productName}
                                                                    onClick = {(e) => {setOpenCartModal(true); addToCart(e.target.name)}}
                                                                    >
                                                                    <Icon name='cart' />
                                                                </Button>
                                                            }  size='small'>
                                                <Header icon='cart' content='Add Item To Cart ?' />
                                                <Modal.Content>
                                                    {
                                                    (user_info_cookie !== null && user_info_cookie !== undefined ) ?
                                                    <p>Product Added To Cart ,  Check Cart from The Navigation Bar On Top to Purchase Item!</p>:
                                                    <>
                                                        <Header as = "h3">Please Log In and Try Again</Header>
                                                        <Button fluid primary onClick = {() => {setToLogin(true)}}>Login In Here</Button>
                                                        <Divider/>
                                                    </>
                                                    }
                                                    
                                                </Modal.Content>
                                                <Modal.Actions>
                                                    <Button basic color='red'  onClick = {() => {setOpenCartModal(false)}}>
                                                        <Icon name='remove' /> Dismiss
                                                    </Button>
                                                </Modal.Actions>
                                        </Modal>
                                    </Grid.Column>
                                    <Grid.Column width={8}>
                                        
                                    <Modal
                                        open = {openWishListModal} 
                                        trigger={
                                                        <Button 
                                                            fluid 
                                                            inverted 
                                                            color = "youtube" 
                                                            centered
                                                            onClick = {() => {setOpenWishListModal(true); AddProductToWishList()}}
                                                            >
                                                            <Icon name='heart' />
                                                        </Button>
                                                    }  size='small'>
                                        <Header icon='heart' content='Add Item To WishList ?' />
                                        <Modal.Content>
                                            {
                                               (user_info_cookie !== null && user_info_cookie !== undefined ) ?
                                               <p>Product Added To WishList ,  Check Wish list from The Navigation Bar On Top !</p>:
                                               <>
                                                <Header as = "h3">Please Log In and Try Again</Header>
                                                <Button fluid primary onClick = {() => {setToLogin(true)}}>Login In Here</Button>
                                                <Divider/>
                                               </>
                                            }
                                            
                                        </Modal.Content>
                                        <Modal.Actions>
                                            <Button basic color='red'  onClick = {() => {setOpenWishListModal(false)}}>
                                                <Icon name='remove' /> Dismiss
                                            </Button>
                                        </Modal.Actions>
                                    </Modal>
                                    </Grid.Column>
                               </Grid.Row>
                                <Grid.Row centered >
                                    
                                    <Grid.Column width={16}>
                                        <ProductDetails
                                            MAIN_IMAGE = { <Image src={MAIN_IMAGE} fluid />}
                                            SECONDARY_IMAGE = {<Image src={SECONDARY_IMAGE} fluid />}
                                            THUMBNAIL = {<Image src={`http://35.208.41.87:8080/getImage/${productName}/2`} fluid />}
                                            reviews = {reviews}
                                        />
                                    </Grid.Column>
                                  
                                </Grid.Row>
                            </Grid>
                        </>
                    </Card.Content>
                    <Card.Content extra >
                        

                    </Card.Content>
                </Card>
                {toLogin ? <Redirect to = "/userLogin"/> : <></>}
        </>
       

    )
}
