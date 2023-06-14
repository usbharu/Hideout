import {Component} from "solid-js";
import {MainPage} from "../templates/MainPage";
import {PostForm} from "../organisms/PostForm";
import {Stack} from "@suid/material";
import {Post} from "../organisms/Post";
import {PostResponse} from "../generated";

export const TopPage: Component = () => {
    return (
        <MainPage>
            <Stack spacing={1}>
                <PostForm label={"投稿する"}/>
                <Post post={{
                    text: "テスト～",
                    sensitive: false,
                    apId: "https://example.com",
                    id: 1234,
                    createdAt: Date.now(),
                    url: "https://example.com",
                    userId: 1234,
                    visibility: "public"
                } as PostResponse}></Post>
                <Post post={{
                    text: "テスト 公開範囲",
                    sensitive: false,
                    apId: "https://example.com",
                    id: 1234,
                    createdAt: 1234567,
                    url: "https://example.com",
                    userId: 1234,
                    visibility: "direct"
                } as PostResponse}></Post>
                <Post post={{
                    text: "テスト～",
                    sensitive: false,
                    apId: "https://example.com",
                    id: 1234,
                    createdAt: 1234567,
                    url: "https://example.com",
                    userId: 1234,
                    visibility: "unlisted"
                } as PostResponse}></Post>
                <Post post={{
                    text: "テスト～",
                    sensitive: false,
                    apId: "https://example.com",
                    id: 1234,
                    createdAt: 1234567,
                    url: "https://example.com",
                    userId: 1234,
                    visibility: "followers"
                } as PostResponse}></Post>
            </Stack>
        </MainPage>
    )
}