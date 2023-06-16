import {Component, For} from "solid-js";
import {CircularProgress} from "@suid/material";
import {Post} from "../organisms/Post";
import {PostDetails} from "../model/PostDetails";

export const PostList: Component<{ posts: PostDetails[] }> = (props) => {
    return (
        <For each={props.posts} fallback={<CircularProgress/>}>
            {
                (item, index) => <Post post={item}/>
            }
        </For>
    )
}
