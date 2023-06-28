import {Component} from "solid-js";
import {Button, List, Stack} from "@suid/material";
import {Home} from "@suid/icons-material";
import {SidebarButton} from "../atoms/SidebarButton";

export const Sidebar: Component = (props) => {
    return (
        <List>
            <SidebarButton text={"AP"} linkTo={"/"}></SidebarButton>
            <SidebarButton text={"Home"} linkTo={"/"}><Home/></SidebarButton>
        </List>
    )
}
