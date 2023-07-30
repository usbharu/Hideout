import {DefaultApiInterface} from "../generated";

export class ApiWrapper {
    api: DefaultApiInterface;

    constructor(initApi: DefaultApiInterface) {
        this.api = initApi;
        console.log(this.api);
        console.log(this.postsGet());
    }

    postsGet = async () => this.api.postsGet()

    usersUserNameGet = async (userName: string) => this.api.usersUserNameGet(userName);

}
