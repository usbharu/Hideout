import {Component, Match, Switch} from "solid-js";
import {Home, Lock, Mail, Public} from "@suid/icons-material";
import {IconButton} from "@suid/material";
import {Visibility} from "../generated";

export const ShareScopeIndicator: Component<{ visibility: Visibility }> = (props) => {
    return <Switch fallback={<Public/>}>
        <Match when={props.visibility == "public"}>
            <IconButton>
                <Public/>
            </IconButton>
        </Match>
        <Match when={props.visibility == "direct"}>
            <IconButton>
                <Mail/>
            </IconButton>
        </Match>
        <Match when={props.visibility == "followers"}>
            <IconButton>
                <Lock/>
            </IconButton>
        </Match>
        <Match when={props.visibility == "unlisted"}>
            <IconButton>
                <Home/>
            </IconButton>
        </Match>
    </Switch>
}
