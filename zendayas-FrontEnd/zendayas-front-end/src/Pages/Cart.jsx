import React from 'react'
import Cookies from "universal-cookie"
import { Segment, Header, Divider, Button, Grid, Image, Table } from "semantic-ui-react"
import NavBar from '../Components/Navbar'

//import wishList_Manager from "../Contexts/Helper_Functions/wishList_management"
import cart_manager from "../Contexts/Helper_Functions/cart_management"


export default function Cart() {

    const cookies = new Cookies();
    let user_info_cookie = cookies.get("USER");

    const [elements, setElements] = React.useState([])

    const [state, setState] = React.useState({ total: 0 })

    React.useEffect(() => {
        fetchCartListItems()

    }, []);

    const fetchCartListItems = async () => {
        if (user_info_cookie !== null && user_info_cookie !== undefined) {
            const result_GET_CART_ITEMS = await cart_manager({
                type: "GET_CART_ITEMS",
                payload: {
                    GCI_username: user_info_cookie.username
                }
            })

            let item_array = [];

            if (result_GET_CART_ITEMS.status === 200) {
                setState({ ...state, total: result_GET_CART_ITEMS.payload.cartTotal })
                Object.keys(result_GET_CART_ITEMS.payload.cartItems).forEach((key) => {
                    //console.log(result_GET_CART_ITEMS.payload.cartItems[key])
                   
                    let item = result_GET_CART_ITEMS.payload.cartItems[key]
                    item_array.push(
                    <Table.Row key = {key}>
                        <Table.Cell>
                            {key}
                        </Table.Cell>
                        <Table.Cell>
                            {item.price.finalPrice}
                        </Table.Cell>
                        <Table.Cell>
                            <Grid>
                                <Grid.Row>
                                    <Grid.Column width = "5">
                                        <Button size = "small" secondary>
                                            +
                                        </Button>
                                    </Grid.Column> 
                                    <Grid.Column width = "6">
                                        <Header as = "h5" textAlign = "center" inverted>
                                            {item.quantity}
                                        </Header>
                                    </Grid.Column>
                                       
                                    <Grid.Column width = "5">
                                        <Button size = "small" secondary>
                                            -
                                        </Button>
                                    </Grid.Column> 
                                </Grid.Row>
                                
                            </Grid>
                        </Table.Cell>
                        <Table.Cell>
                            {Number(item.price.finalPrice) * Number(item.quantity)}
                        </Table.Cell>
                        <Table.Cell>
                            <Button negative fluid>Remove</Button>
                        </Table.Cell>
                    </Table.Row>
                   )
                   
                })

                setElements(item_array)
            }


        }

    }
    return (
        <NavBar>
            <Segment>
                <Header as="h1">Cart</Header>
                <Divider />
                <Segment>
                    <Grid>
                        <Grid.Row>
                            <Grid.Column width="16">
                                <Header as="h2">Total  </Header>
                            </Grid.Column>
                        </Grid.Row>
                        <Grid.Row>
                            <Grid.Column width="16">
                                <Header as="h2">LKR {state.total}.00</Header>
                            </Grid.Column>
                        </Grid.Row>
                        <Grid.Row>
                            <Grid.Column width="16">
                                <Button
                                    positive
                                    fluid
                                    disabled={state.total === 0}
                                >
                                    Proceed To Checkout
                                </Button>
                            </Grid.Column>
                        </Grid.Row>
                    </Grid>
                </Segment>
                <Segment>
                    <Table inverted celled>
                        <Table.Header fullWidth>
                        </Table.Header>
                        <Table.Header>
                            <Table.Row>
                                <Table.HeaderCell>Name</Table.HeaderCell>
                                <Table.HeaderCell>Unit Price</Table.HeaderCell>
                                <Table.HeaderCell>Qty</Table.HeaderCell>
                                <Table.HeaderCell>final price</Table.HeaderCell>
                                <Table.HeaderCell>Remove From Cart</Table.HeaderCell>
                            </Table.Row>
                        </Table.Header>

                        <Table.Body>
                            {
                                elements
                            }
                        </Table.Body>
                    </Table>
                </Segment>

            </Segment>
        </NavBar>
    )
}
