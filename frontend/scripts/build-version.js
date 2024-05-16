import { readFileSync } from "fs";

import { write } from "./dist.mjs";
import { log } from "./log.mjs";


const packageObj = JSON.parse(readFileSync(new URL("../../package.json", import.meta.url)).toString());

write("./version.txt", packageObj.version);

log("Copied current version reference");