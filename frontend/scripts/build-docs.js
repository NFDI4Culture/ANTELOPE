import { fileURLToPath } from "url";
import { readdirSync, readFileSync } from "fs";
import { join } from "path";

import * as commonmark from "commonmark";

import { write } from "./dist.mjs";
import { log } from "./log.mjs";


const __dirname = fileURLToPath(new URL(".", import.meta.url));

const DOCS_DIR = join(__dirname, "../../documentation/");
const ARTICLES = readdirSync(DOCS_DIR, {
    withFileTypes: true
})
.filter(dirent => dirent.isFile())
.filter(dirent => /^\d+\. *.+\.md$/.test(dirent.name))
.map(dirent => {
    const name = dirent.name
    .replace(/^\d+\. */, "")
    .replace(/\.md$/, "");
    return {
        targetName: name
                .replace(/^\d+\. */, "")
                .replace(/ +/g, "-")
                .replace(/\.md$/, "")
                .toLowerCase(),
        displayName: name
                .replace(/^\d+\. */, "")
                .replace(/[ _-]+/g, " ")
                .replace(/\.md$/, ""),
        path: join(DOCS_DIR, dirent.name)
    };
});


const reader = new commonmark.Parser();
const writer = new commonmark.HtmlRenderer();

ARTICLES.forEach(article => {
    const html = writer.render(reader.parse(readFileSync(article.path).toString()));
    
    write(join("./docs/", `_${article.targetName}.htm`), html);
});

write(
    join("./docs/", "structure.json"),
    JSON.stringify(
        ARTICLES.map(article => {
            return {
                targetName: article.targetName,
                displayName: article.displayName
            };
        })
    , null, 2)
);


log(`${ARTICLES.length} documentation files build`);