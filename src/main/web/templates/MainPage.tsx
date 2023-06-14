import {ParentComponent} from "solid-js";
import {Grid} from "@suid/material";
import {Sidebar} from "./Sidebar";

export const MainPage: ParentComponent = (props) => {

    return (
        <Grid container spacing={2}>
            <Grid item xs={0} md={3}>
                <Sidebar/>
            </Grid>
            <Grid item xs={12} md={5}>
                {props.children}
            </Grid>
            <Grid item xs={0} md={3}>

            </Grid>
        </Grid>
    )
}