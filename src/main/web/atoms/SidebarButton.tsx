import {ParentComponent} from "solid-js";
import {Button, ListItem, ListItemAvatar, ListItemButton, ListItemIcon, ListItemText} from "@suid/material";
import {Link} from "@solidjs/router";

export const SidebarButton: ParentComponent<{ text: string,linkTo:string }> = (props) => {
    return (
        <ListItem>
            <ListItemButton component={Link} href={props.linkTo}>
                <ListItemIcon>{props.children}</ListItemIcon>
                <ListItemText primary={props.text}/>
            </ListItemButton>
        </ListItem>
    )
}
