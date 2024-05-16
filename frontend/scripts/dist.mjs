import { fileURLToPath } from "url";
import { mkdirSync, writeFileSync, cpSync } from "fs";
import { join, normalize } from "path";


const __dirname = fileURLToPath(new URL(".", import.meta.url));

function preparePath(targetRootPath, relativePath) {
    const absolutePath = join(__dirname, targetRootPath, relativePath);

    mkdirSync(absolutePath.replace(/[^/]+$/, ""), {
        recursive: true
    });

    return absolutePath;
}

function cp(absoluteSourcePath, absoluteTargetPath) {
    if(normalize(absoluteSourcePath) === normalize(absoluteTargetPath)) return;

    cpSync(absoluteSourcePath, absoluteTargetPath);
}


export function copy(absoluteSourcePath, relativeTargetPath) {
    cp(absoluteSourcePath, preparePath("../assets/", relativeTargetPath));
    cp(absoluteSourcePath, preparePath("../dist/assets/", relativeTargetPath));
};

export function write(relativeTargetPath, data) {
    writeFileSync(preparePath("../assets/", relativeTargetPath), data);
    writeFileSync(preparePath("../dist/assets/", relativeTargetPath), data);
};