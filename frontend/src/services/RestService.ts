import { EnvService } from "./EnvService";

type TAtomic = string|number|boolean;
type TSearchParams = { [ key: string ]: TAtomic; };
type THeaders = { [ key: string ]: string; };


export class Request<T> {
    private static MOCK_FALLBACK_ENABLED: boolean = true;	// Set to false to test failure behaviour without mock intercept

    private static baseUrl = `${document.location.origin.replace(/(:\d+)?$/, ":8080")}/api`;

	private readonly method: string;
	private readonly endpoint: string;
	private readonly searchParams?: TSearchParams;
	private readonly body?: unknown;
	private readonly headers?: THeaders;

	private mockResponse?: T;

	constructor(method: string, endpoint: string, searchParams?: TSearchParams, body?: unknown, headers?: THeaders) {
		this.method = method;
		this.endpoint = endpoint;
		this.searchParams = searchParams;
		this.body = body;
		this.headers = headers;
	}
	
    public call(): Promise<T> {
		console.log(this.body)
		return new Promise((resolve, reject) => {
			const url = `${Request.baseUrl}${this.endpoint.replace(/^\/?/, "/")}${
				this.searchParams
				? `?${
					Object.entries(this.searchParams)
					.map((param: [ string, TAtomic ]) => `${encodeURI(param[0])}=${encodeURI(param[1].toString())}`)
					.join("&")
				}`
				: ""
			}`;
			fetch(url, {
				method: this.method.toUpperCase(),
				mode: "cors",
				cache: "no-cache",
				headers: {
					...!(this.body instanceof FormData) ? { "Content-Type": "application/json" } : {},
										
					...this.headers ?? {}
				},
				
				... this.body
				? {
					body: !(this.body instanceof FormData) ? JSON.stringify(this.body): this.body
				}
				: {}
			})
			.then(res => res.json())
			.then(obj => {
				resolve(obj as T);
			})
			.catch(err => {
				if(EnvService.IS_DEV
				&& Request.MOCK_FALLBACK_ENABLED
				&& this.mockResponse) {
					console.log(`${
						this.method.toUpperCase()
					}: ${
						url
					}${this.body ? ":" : ""}`);
					this.body && console.log(this.body);
					
					console.warn("Request has assigned a development mock response that is used in place of applying the default behavior.");

					setTimeout(() => resolve(this.mockResponse as T), 1000);

					return;
				}

				reject(err);
			});
		});
    }

	public mock(fallbackResponse: T): this {
		this.mockResponse = fallbackResponse;

		return this;
	}
}


export const RestService = {
	GET: (endpoint: string, searchParams?: TSearchParams, headers?: THeaders) => new Request("GET", endpoint, searchParams, headers),
	POST: (endpoint: string, searchParams?: TSearchParams, body?: unknown, headers?: THeaders) => new Request("POST", endpoint, searchParams, body, headers)
};