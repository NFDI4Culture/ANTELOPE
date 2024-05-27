const { spawn } = require("child_process");
const { join } = require("path");


// https://gist.github.com/t-ski/eca16abcd630e1205530fe388e67204e
class AsyncMutex {
	constructor() {
		this.acquireQueue = [];
		this.isLocked = false;
	}
  	
	lock(callback) {
		return new Promise(resolveOuter => {
			new Promise(resolveInner => {
				if(this.isLocked) {
					this.acquireQueue.push(resolveInner);
					return;
				}
				this.isLocked = true;
				resolveInner();
			})
			.then(() => {
				const callbackResults = callback();
				((callbackResults instanceof Promise)
				? callbackResults
				: Promise.resolve(callbackResults))
				.then(results => {
					this.isLocked = !!this.acquireQueue.length;
					(this.acquireQueue.shift() || (() => {}))();
					resolveOuter(results);
				});
			});
		});
  	}
}


const logMutex = new AsyncMutex();

let lastLogW;

function startScript(workspace, name = "start:dev") {
    console.log(`\x1b[1m\x1b[34mSTART\x1b[22m ${name}@${workspace}:\x1b[0m`);

    const log = (channel, message) => {
        logMutex.lock(() => {
            channel.write((lastLogW !== workspace) ? `\n\x1b[1m\x1b[34m> ${workspace}\x1b[22m: ${"–".repeat(30 - workspace.length)}\x1b[0m\n${message.replace(/^(\s*\n)?/, "\n")}` : message);
            lastLogW = workspace;
        });
    };

    const child = spawn("npm", [
        "run", name, "-w", workspace
    ], {
        cwd: join(__dirname, "../")
    });
    child.stdout.on("data", data => log(process.stdout, data.toString()));
    child.stderr.on("data", data => log(process.stderr, data.toString()));
}


startScript("frontend");
startScript("backend");