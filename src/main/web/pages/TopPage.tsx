import {Component, createResource} from "solid-js";
import {MainPage} from "../templates/MainPage";
import {PostForm} from "../organisms/PostForm";
import {Stack} from "@suid/material";
import {DefaultApi} from "../generated";
import {PostList} from "../templates/PostList";
import {ApiWrapper} from "../lib/ApiWrapper";


export const TopPage: Component = () => {
    const api = new ApiWrapper(new DefaultApi())
    const [posts] = createResource(api.postsGet);

    return (
        <MainPage>
            <Stack spacing={1} alignItems={"stretch"}>
                <PostForm label={"投稿する"}/>
                <PostList posts={posts() ?? []}/>
            </Stack>
        </MainPage>
    )
}
