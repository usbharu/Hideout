import {PostResponse, UserResponse} from "../generated";

export type PostDetails = PostResponse & {
    user: UserResponse
}
