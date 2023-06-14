import {Avatar as SuidAvatar} from "@suid/material";
import {Component, JSXElement} from "solid-js";

export const Avatar: Component<{ src: string }> = (props): JSXElement => {
    return (
        <SuidAvatar src={props.src}/>
    )
}