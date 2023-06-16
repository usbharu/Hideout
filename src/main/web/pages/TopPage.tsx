import {Component, createResource} from "solid-js";
import {MainPage} from "../templates/MainPage";
import {PostForm} from "../organisms/PostForm";
import {Stack} from "@suid/material";
import {DefaultApi} from "../generated";
import {PostDetails} from "../model/PostDetails";
import {PostList} from "../templates/PostList";
import {ApiWrapper} from "../lib/ApiWrapper";


export const TopPage: Component = () => {
    const api = new ApiWrapper(new DefaultApi())
    const [posts] = createResource(api.postsGet);

    return (
        <MainPage>
            <Stack spacing={1} alignItems={"stretch"}>
                <PostForm label={"投稿する"}/>
                <PostList posts={posts()?.map(value => {
                    return {
                        ...value,
                        user: {
                            id: 1234,
                            createdAt: Date.now(),
                            domain: "test-hideout.usbharu.dev",
                            name: "test",
                            url: "https://test-hideout.usbharu.dev",
                            screenName: "test",
                            description: ""
                        }
                    } as PostDetails
                }) ?? []}/>
            </Stack>
        </MainPage>
    )
}
