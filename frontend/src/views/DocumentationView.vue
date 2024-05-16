<script setup lang="ts">
	import { Ref, ref } from "vue";

	import { EnvService } from "../services/EnvService";
	import DevTagComponent from "../components/DevTagComponent.vue";
	import ButtonComponent from "../components/ButtonComponent.vue";
	import PlaceholderComponent from "../components/PlaceholderComponent.vue";
	import FooterComponent from "../components/FooterComponent.vue";

	window.addEventListener("popstate", e => {
		(isNaN(e.state))
		? (activeIndex.value = -1)
		: loadArticle(e.state, true);
	});

	const error: Ref<string> = ref(null);
	const articles: Ref<{
		targetName: string;
		displayName: string;
	}[]> = ref([]);
	const articleEl: Ref<HTMLElement> = ref(null);
	const activeIndex = ref(-3);

	fetch(`/assets/docs/structure.json`)
	.then(res => res.json())
	.then(async structure => {
		articles.value = structure;
		
		const urlArticle = document.location.search
		.replace(/^\?/, "");
		
		if(!urlArticle) {
			activeIndex.value = -1;

			return;
		}
		
		const index = articles.value
		.map((article, i) => (article.targetName == urlArticle) ? i : null)
		.filter(index => index !== null)
		.pop();

		const initHash = document.location.hash;
		await loadArticle(index, true);
		setTimeout(() => {
			scrollAnchor(initHash);
		}, 0);
	})
	.catch(err => {
		EnvService.IS_DEV && console.error(err);
		
		displayError();
	});

	function loadArticle(index, isInit = false): Promise<void> {
		return new Promise(resolve => {
			if(!articles.value[index]) {
				displayError("Article does not exist");

				resolve();
				return;
			}

			const targetName: string = articles.value[index].targetName;

			fetch(`/assets/docs/_${targetName}.htm`)
			.then(res => res.text())
			.then(text => {
				articleEl.value.innerHTML = text;

				window.scrollTo(0, 0);
				
				const targetUrl = `${document.location.pathname}?${targetName}`;
				isInit
				? history.replaceState(index, "", targetUrl)
				: history.pushState(index, "", targetUrl);

				activeIndex.value = index;

				setTimeout(() => {
					const ids: string[] = [];
					Array.from(
						articleEl.value
						.querySelectorAll("h2, h3")
					).forEach(hEl => {
						const coreId = hEl.textContent
						.trim()
						.toLowerCase()
						.replace(/[^\w\d ]+/g, "")
						.replace(/ +/g, "-");
						let id = coreId;
						let i = 0;
						while(ids.includes(id) && i < 50) {
							id = `${coreId}-${i}`;
						}
						ids.push(coreId);
						const aEl = document.createElement("a");
						aEl.addEventListener("click", () => scrollAnchor(id)); 
						aEl.textContent = hEl.textContent;
						hEl.textContent = "";
						hEl.appendChild(aEl);
						hEl.setAttribute("id", id);
					});

					resolve();
				}, 0);
			})
			.catch(err => {
				EnvService.IS_DEV && console.error(err);

				displayError();

				resolve();
			});
		});
	}

	function scrollAnchor(hashOrId) {
		if(!hashOrId || activeIndex.value < 0) return;

		const id = hashOrId.replace(/^#?/, "");
		
		history.replaceState(activeIndex.value, "",
			`${document.location.pathname}?${articles.value[activeIndex.value].targetName ?? ""}#${id}`
		);

		const hEl: HTMLElement = articleEl.value.querySelector(`#${id}`);
		window.scrollTo(0, hEl.offsetTop + hEl.parentElement.scrollTop);
	}

	function displayError(message: string = "Try again later") {
		error.value = message;

		activeIndex.value = -2;
	}
</script>

<template>
	<main class="docs">
		<header class="docs-sidebar">
			<div class="docs-logo">
				<svg viewBox="0 0 155 145" xmlns="http://www.w3.org/2000/svg">
					<path d="M 92.915 0 C 58.63 0 30.839 27.776 30.839 62.04 C 30.839 96.304 58.63 124.093 92.915 124.093 C 127.196 124.093 155 96.317 155 62.04 C 154.987 27.776 127.196 0 92.915 0 Z" style="fill: rgb(249, 205, 14);"/>
					<path class="contrast" d="M 138.485 60.205 C 130.237 54.596 126.778 50.289 125.283 47.604 C 123.787 44.918 121.584 40.007 119.35 37.588 C 111.382 28.939 96.252 30.393 99.494 15.247 C 90.943 35.433 120.187 31.533 118.846 46.568 C 117.084 40.145 109.835 38.03 109.835 38.03 C 109.835 38.03 108.253 45.539 116.107 48.959 C 105.577 55.431 107.884 71.945 67.729 68.779 C 50.824 67.448 48.633 72.959 44.541 75.794 C 38.966 79.658 32.668 79.456 32.668 79.456 C 38.561 80.962 41.5 80.748 44.68 79.721 C 44.047 80.316 43.452 80.962 42.893 81.672 C 36.064 90.535 35.073 99.808 22.341 101.821 C 22.341 101.821 15.791 111.015 9.366 123.021 C 5.841 129.593 2.359 137.015 0 144.22 C 3.447 137.598 7.2 131.404 11.178 125.691 C 24.38 106.734 40.104 93.084 55.386 86.826 C 72.733 79.737 83.277 79.151 87.181 80.887 C 88.535 81.48 91.081 86.229 83.162 88.561 C 74.166 91.208 73.43 89.851 65.486 91.107 C 48.127 93.868 28.194 105.685 14.914 128.403 C 32.009 107.811 47.873 99.44 60.216 96.121 C 79.843 90.829 88.168 100.252 111.114 89.917 C 111.114 89.917 102.258 104.479 79.715 111.039 C 117.551 105.228 122.71 79.532 122.71 79.532 C 122.71 79.532 109.166 87.56 107.325 82.495 C 105.793 78.253 113.053 76.391 120.721 60.068 C 123.925 58.838 131.949 64.967 138.46 60.205 L 138.485 60.205 Z" style=""/>
				</svg>
				<h1>Documentation</h1>
				<DevTagComponent />
			</div>
			<nav class="docs-nav">
				<ButtonComponent small>
					<RouterLink to="/">Go to Application</RouterLink>
				</ButtonComponent>
				<ol>
					<li v-for="(article, index) in articles" :key="article.targetName">
						<a :class="(activeIndex == index) ? 'active' : ''" @click="loadArticle(index)">{{ article.displayName }}</a>
					</li>
				</ol>
			</nav>
		</header>
		<article class="docs-article">
			<section class="wrapper" v-show="activeIndex >= 0 || activeIndex == -3" ref="articleEl">
				<br>
				<PlaceholderComponent />
			</section>
			<section class="wrapper" v-show="activeIndex == -1">
				<h1 class="large">Documentation</h1>
				<p>This is the official documentation of the ANTELOPE project.</p>
				<small>
					<a href="https://gitlab.com/nfdi4culture/ta5-knowledge-graph/annotation-service/-/tree/main/documentation?ref_type=heads" target="_blank">Discovered an error? Help us fix it!</a>
				</small>
			</section>
			<section class="wrapper" v-show="activeIndex == -2">
				<h2>{{ error }}</h2>
				<p>
					<a @click="loadArticle(0)">Start from here</a>
				</p>
			</section>
			<div class="docs-pagination">
				<div :class="(activeIndex > 0) ? '' : 'hide'">
					<span>Previous Article</span>
					<a @click="loadArticle(activeIndex - 1)">{{ articles[activeIndex - 1]?.displayName }}</a>
				</div>
				<div :class="(activeIndex >= -1 && activeIndex < articles.length - 1) ? '' : 'hide'">
					<span>Next Article</span>
					<a @click="loadArticle(activeIndex + 1)">{{ articles[activeIndex + 1]?.displayName }}</a>
				</div>
			</div>
			<FooterComponent />
		</article>
	</main>
</template>

<style lang="scss">
	@import "@/assets/scss/shared";

	.docs section * {
		max-width: none !important;
	}
	.docs section {
		font-family: "Roboto";
    	letter-spacing: 0;

		h1, h2, h3, h4, h5, h6 {
			font-family: "Roboto Condensed";
    		letter-spacing: 0.05px;
		}
		h2, h3, h4 {
			&[id] a {				
				@include icon("anchor");
				
				&::after {
					display: none;
					margin-left: var(--space-xs) !important;
				}

				&:hover::after {
					display: block
				}
			}
		}
	}
</style>
<style lang="scss" scoped>
	@import "@/assets/scss/shared";

	main {
		display: flex;
		flex-direction: row;
		align-items: flex-start;
		flex: 1 0 auto;
		width: 100%;
	}

	.docs {
		& > * {
			min-height: 100vh;
		}
		
		&-sidebar {
			position: sticky;
			display: flex;
			flex-direction: column;
			align-items: flex-start;
			top: 0;
			min-width: 300px;
			background-color: var(--color-bg-gray);
		}
		&-nav {
			margin-top: var(--space-m);
			// padding-left: calc($logoSize + var(--space-m));
			color: var(--color-fg-gray);

			a {
				&:hover {
					filter: brightness(0.8);
				}
			}
			ol {
				list-style: none;
				margin-top: var(--space-m);
				padding-left: 0;

				li {
					&:not(:last-child) {
						margin-bottom: var(--space-s);
					}

					a {
						&.active {
							color: var(--color-fg-dark);
							text-shadow: 0.25px 0 0 var(--color-fg-gray);
						}
					}
				}
			}
		}
		&-logo {
			display: flex;
			flex-direction: row;
			align-items: center;

			svg {
				margin-bottom: -0.5em;
				width: 4rem;
			}
			h1 {
				margin-left: var(--space-s);
				text-transform: uppercase;
				
				@include font-l;
			}
		}
		&-article {
			flex-direction: column;
			flex: 1 0 0;
			display: flex;
			padding-top: calc(var(--space-m) + var(--space-xs));
			padding-left: var(--space-l);
			padding-bottom: 0;

			section {
				flex: 1 0 auto;
			}
		}
		&-pagination {
			display: flex;
			flex-direction: row;
			align-items: flex-start;
			justify-content: space-between;
			margin-top: var(--space-l);
			padding-top: var(--space-m);
			border-top: 1px solid var(--color-bg-gray);

			& > * {
				display: flex;
				flex-direction: column;
				min-width: 300px;

				&.hide {
					visibility: hidden;
				}

				span {
					display: flex;
					flex-direction: row;
					align-items: center;
					margin-bottom: var(--space-xs);
					color: var(--color-fg-gray);
					font-weight: 600;
					text-transform: uppercase;
					
					@include font-s;
				}
				a {
					color: var(--color-secondary);
				}

				&:first-child {
					align-items: flex-start;

					span {
						@include icon("arrow-l", 0.75em, "before");
					}
				}
				&:last-child {
					align-items: flex-end;

					span {
						@include icon("arrow-r", 0.75em);
					}
				}
			}
		}
	}
</style>