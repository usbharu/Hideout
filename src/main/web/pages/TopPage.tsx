import {Component} from "solid-js";
import {MainPage} from "../templates/MainPage";
import {PostForm} from "../organisms/PostForm";
import {Stack} from "@suid/material";
import {PostResponse} from "../generated";
import {PostList} from "../templates/PostList";
import {useApi} from "../lib/ApiProvider";
import {createStore} from "solid-js/store";


export const TopPage: Component = () => {
    const api = useApi()
    const [posts, setPosts] = createStore<PostResponse[]>([])
    api().postsGet().then((res)=>setPosts(res))

    return (
        <MainPage>
            <Stack spacing={1} alignItems={"stretch"}>
                <PostForm label={"投稿する"}/>
                <PostList posts={posts}/>
            </Stack>
        </MainPage>
    )
}
