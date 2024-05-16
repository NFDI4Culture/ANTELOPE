export function log(message) {
    console.log(`\x1b[32m${message.replace(/([^.!?]?)$/, "$1.")}\x1b[0m`);
};