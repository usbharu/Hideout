import {createContextProvider} from "@solid-primitives/context";
import {createSignal} from "solid-js";
import {DefaultApi, DefaultApiInterface} from "../generated";

export const [ApiProvider,useApi] = createContextProvider((props:{api:DefaultApiInterface}) => {
   const [api,setApi] = createSignal(props.api);
   return api
},()=>new DefaultApi());
