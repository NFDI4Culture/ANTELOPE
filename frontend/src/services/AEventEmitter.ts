export abstract class AEventEmitter<T> {
    private prefix: string;

    constructor(uniquePrefix: string) {
        this.prefix = uniquePrefix;
    }

    private getUniqueEvent(event: string): string {
        return `${this.prefix}:${event}`;
    }
    
    protected dispatchEvent(event: string, data?: T) {
        document.dispatchEvent(new CustomEvent(this.getUniqueEvent(event), {
            detail: data
        }));
    }

    public on(event: string, callback: (data?: T) => void) {
        document.addEventListener(this.getUniqueEvent(event), (e) => {
            callback((e as unknown as { detail: T; }).detail);
        });
    }
}