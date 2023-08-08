import {Component, For} from "solid-js";
import {CircularProgress} from "@suid/material";
import {Post} from "../organisms/Post";
import {PostResponse} from "../generated";

export const PostList: Component<{ posts: PostResponse[] | undefined }> = (props) => {
    return (
        <For each={props.posts} fallback={<CircularProgress/>}>
            {
                (item, index) => <Post post={item}/>
            }
        </For>
    )
}
