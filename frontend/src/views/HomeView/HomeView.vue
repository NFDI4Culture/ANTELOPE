<script setup lang="ts">
	import { onMounted, Ref, ref } from "vue";
	import { RouterLink } from "vue-router";

	import { ResultsService } from "../../services/ResultsService";
	import { EntitySelectionService } from "../../services/EntitySelectionService";
	import DevTagComponent from "../../components/DevTagComponent.vue";
	import OverlayComponent from "../../components/OverlayComponent.vue";
	import TabsComponent from "../../components/TabsComponent.vue";
	import FooterComponent from "../../components/FooterComponent.vue";
	import SidebarComponent from "./SidebarComponent.vue";
	import SearchComponent from "./search/SearchComponent.vue";
	import IntegrationOverlay from "./overlays/IntegrationOverlay.vue";

	const showSidebar = ref(false);
	const showSidebarNotification = ref(false);
	const showUncollapsed = ref(true);
	const isScrolled = ref(false);

	const mainEl: Ref<HTMLElement> = ref(null);
	const integrationOverlayEl: Ref<OverlayComponent> = ref(null);

	let toggleSidebarOnSelect: boolean = true;

	ResultsService.on("define", collapse);

	EntitySelectionService.on("select", () => {
		showSidebarNotification.value = !showSidebar.value;

		if(!toggleSidebarOnSelect || showSidebar.value) return;
		toggleSidebarOnSelect = false;

		toggleSidebar();
	});

	window.addEventListener("scroll", checkScroll);
	window.addEventListener("resize", calcMainWidth);

	onMounted(checkScroll);
	onMounted(calcMainWidth);

	function calcMainWidth() {
		(document.querySelector(":root") as HTMLElement)
		.style.setProperty("--main-width", `${mainEl.value.offsetWidth}px`);
	}

	function checkScroll() {
		isScrolled.value = window.scrollY > 0;
	}

	function toggleSidebar() {
		showSidebar.value = !showSidebar.value;

		showSidebarNotification.value = false;

		calcMainWidth();
		setTimeout(calcMainWidth, 0);
	}

	function collapse() {
		showUncollapsed.value = false;
	}
	function uncollapse() {
		showUncollapsed.value = true;
	}
</script>

<template>
	<main>
		<div class="content wrapper" :class="showSidebar ? 'right' : ''" ref="mainEl">
			<header class="header" :class="isScrolled ? 'scroll' : ''">
				<h1>
					Annotation, Terminology Lookup and Personalization
					<DevTagComponent />
				</h1>
				<nav>
					<ol>
						<li><a @click="integrationOverlayEl.toggle()">Integration</a></li>
						<li><a href="/api.html" target="_blank">API</a></li>
						<li class="scroll-keep desktop"><a class="header-button--selection" :class="[ showSidebar ? 'active' : '', showSidebarNotification ? 'notify' : '' ]" @click="toggleSidebar()">Your Selection</a></li>
					</ol>
				</nav>
			</header>
			<section class="hero" :class="showUncollapsed ? 'uncollapse' : ''">
				<svg class="hero-logo" :viewBox="showUncollapsed ? '0 0 500 215' : '0 0 500 140'" xmlns="http://www.w3.org/2000/svg">
					<path class="fg-dark" v-show="showUncollapsed" d="M 39.898 203.613 L 18.964 203.664 L 15.623 213.465 L 0 213.502 L 21.318 157.112 L 37.319 157.075 L 58.912 213.364 L 43.287 213.401 L 39.898 203.625 L 39.898 203.613 Z M 35.984 192.492 L 29.356 173.749 L 22.826 192.516 L 35.984 192.479 L 35.984 192.492 Z" fill="black" style=""/>
					<path class="fg-dark" v-show="showUncollapsed" d="M 80.293 213.302 L 80.158 156.961 L 94.81 156.924 L 121.961 191.306 L 121.875 156.86 L 136.451 156.823 L 136.589 213.165 L 122.01 213.202 L 94.86 178.817 L 94.946 213.263 L 80.293 213.302 Z" fill="black" style=""/>
					<path class="fg-dark" v-show="showUncollapsed" d="M 184.627 169.102 L 184.741 213.039 L 170.088 213.076 L 169.974 169.139 L 158.736 169.163 L 158.736 156.759 L 196.665 156.66 L 196.665 169.065 L 184.627 169.09 L 184.627 169.102 Z" fill="black" style=""/>
					<path class="fg-dark" v-show="showUncollapsed" d="M 250.193 168.939 L 232.772 168.976 L 232.796 178.391 L 249.246 178.353 L 249.27 190.758 L 232.821 190.795 L 232.845 200.509 L 250.267 200.472 L 250.292 212.876 L 218.217 212.95 L 218.082 156.609 L 250.154 156.535 L 250.181 168.939 L 250.193 168.939 Z" fill="black" style=""/>
					<path class="fg-dark" v-show="showUncollapsed" d="M 290.317 156.439 L 290.431 200.376 L 306.753 200.339 L 306.753 212.755 L 275.815 212.817 L 275.677 156.476 L 290.33 156.439 L 290.317 156.439 Z" fill="black" style=""/>
					<path class="fg-dark" v-show="showUncollapsed" d="M 321.806 184.518 C 321.806 180.334 322.553 176.438 324.096 172.824 C 325.643 169.212 327.774 166.05 330.504 163.36 C 333.233 160.656 336.508 158.552 340.322 157.021 C 344.134 155.488 348.322 154.717 352.906 154.717 C 357.492 154.717 361.629 155.451 365.467 156.971 C 369.305 158.478 372.617 160.582 375.383 163.26 C 378.165 165.937 380.32 169.088 381.877 172.7 C 383.434 176.312 384.206 180.195 384.219 184.392 C 384.219 188.577 383.472 192.476 381.926 196.088 C 380.393 199.698 378.238 202.864 375.485 205.554 C 372.731 208.243 369.428 210.359 365.59 211.891 C 361.753 213.423 357.565 214.197 353.03 214.197 C 348.497 214.197 344.247 213.46 340.434 211.94 C 336.622 210.436 333.331 208.329 330.59 205.652 C 327.836 202.974 325.692 199.824 324.136 196.214 C 322.577 192.6 321.806 188.715 321.793 184.518 L 321.806 184.518 Z M 337.132 184.482 C 337.132 186.723 337.568 188.789 338.415 190.684 C 339.262 192.576 340.42 194.221 341.865 195.603 C 343.312 196.997 344.994 198.068 346.925 198.84 C 348.844 199.612 350.876 199.987 353.018 199.987 C 355.162 199.987 357.194 199.588 359.112 198.814 C 361.031 198.043 362.726 196.948 364.183 195.554 C 365.654 194.156 366.8 192.513 367.647 190.607 C 368.494 188.715 368.906 186.649 368.906 184.405 C 368.906 182.164 368.469 180.097 367.622 178.203 C 366.775 176.312 365.602 174.669 364.132 173.285 C 362.661 171.89 360.955 170.819 359.036 170.048 C 357.117 169.274 355.088 168.902 352.944 168.902 C 350.799 168.902 348.77 169.298 346.851 170.072 C 344.932 170.843 343.25 171.94 341.816 173.336 C 340.371 174.73 339.225 176.373 338.39 178.279 C 337.555 180.183 337.132 182.24 337.132 184.482 Z" fill="black" style=""/>
					<path class="fg-dark" v-show="showUncollapsed" d="M 419.444 212.438 L 404.79 212.477 L 404.653 156.136 L 427.979 156.075 C 434.309 156.059 439.156 157.693 442.533 160.969 C 445.91 164.243 447.604 168.876 447.617 174.853 C 447.629 180.831 445.959 185.464 442.607 188.765 C 439.254 192.066 434.409 193.721 428.08 193.733 L 419.407 193.758 L 419.457 212.438 L 419.444 212.438 Z M 419.37 181.952 L 424.23 181.952 C 429.611 181.927 432.291 179.585 432.291 174.89 C 432.291 170.195 429.587 167.881 424.202 167.893 L 419.343 167.893 L 419.382 181.952 L 419.37 181.952 Z" fill="black" style=""/>
					<path class="fg-dark" v-show="showUncollapsed" d="M 499.902 168.303 L 482.48 168.34 L 482.504 177.757 L 498.953 177.718 L 498.977 190.122 L 482.532 190.159 L 482.557 199.873 L 499.975 199.837 L 500 212.24 L 467.927 212.315 L 467.79 155.976 L 499.862 155.9 L 499.886 168.303 L 499.902 168.303 Z" fill="black" style=""/>
					<path d="M 249.995 -0.198 C 216.088 -0.198 188.603 27.272 188.603 61.158 C 188.603 95.045 216.088 122.528 249.995 122.528 C 283.898 122.528 311.396 95.058 311.396 61.158 C 311.383 27.272 283.898 -0.198 249.995 -0.198 Z" style="fill: rgb(249, 205, 14);"/>
					<path class="contrast" d="M 295.063 59.344 C 286.906 53.796 283.485 49.537 282.006 46.881 C 280.527 44.225 278.348 39.368 276.139 36.976 C 268.259 28.422 253.295 29.86 256.502 14.881 C 248.045 34.845 276.967 30.988 275.64 45.857 C 273.898 39.505 266.729 37.413 266.729 37.413 C 266.729 37.413 265.164 44.839 272.932 48.222 C 262.518 54.622 264.799 70.954 225.087 67.823 C 208.368 66.507 206.201 71.957 202.154 74.761 C 196.641 78.582 190.412 78.382 190.412 78.382 C 196.24 79.872 199.147 79.66 202.292 78.645 C 201.666 79.233 201.077 79.872 200.524 80.574 C 193.771 89.339 192.791 98.51 180.199 100.501 C 180.199 100.501 173.721 109.594 167.367 121.467 C 163.881 127.967 160.437 135.307 158.104 142.433 C 161.513 135.884 165.225 129.758 169.159 124.108 C 182.215 105.36 197.766 91.86 212.88 85.671 C 230.036 78.66 240.463 78.081 244.324 79.798 C 245.663 80.384 248.181 85.081 240.35 87.387 C 231.453 90.005 230.725 88.663 222.868 89.905 C 205.701 92.636 185.987 104.322 172.854 126.79 C 189.76 106.425 205.449 98.146 217.656 94.864 C 237.067 89.63 245.3 98.949 267.994 88.728 C 267.994 88.728 259.235 103.13 236.941 109.617 C 274.36 103.87 279.462 78.458 279.462 78.458 C 279.462 78.458 266.067 86.397 264.246 81.388 C 262.731 77.193 269.911 75.351 277.495 59.208 C 280.663 57.992 288.599 64.053 295.038 59.344 L 295.063 59.344 Z" style=""/>
				</svg>
				<p v-show="showUncollapsed" class="hero-description">
					ANTELOPE is a powerful free, open source service for easy data annotation: terminology search; entity linking; and integration with research data management systems.
				</p>
				<SearchComponent />
			</section>
			<FooterComponent />
		</div>
		<aside v-show="showSidebar" class="desktop">
			<SidebarComponent />
		</aside>
	</main>
	<OverlayComponent ref="integrationOverlayEl">
		<IntegrationOverlay />
	</OverlayComponent>
</template>

<style lang="scss" scoped>
	@import "@/assets/scss/shared";

	main {
		display: flex;
		flex-direction: row;
		width: 100%;
	}
	aside {
		position: sticky;
        display: block;
		flex: 1 0 auto;
		top: 0;
		min-width: 300px;
		max-width: 600px;
		height: 100vh;
		background-color: var(--color-bg-gray);
		overflow-x: hidden;
		overflow-y: scroll;
        z-index: 10;
	}
	
	.content {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding-top: 0;
		padding-bottom: 0;

		&.right {
			margin-right: 0;
		}
	}
	
	.header {
		position:  sticky;
		top: calc(var(--space-m) - 0.5rem);
		pointer-events: none;
		z-index: 100;

		@include nav;
		// @include font-m;
		
		&.scroll {
			h1, li {
				&:not(.scroll-keep) {
					transform: rotateX(-90deg);
					pointer-events: auto;
					user-select: none;
				}
			}
		}
		h1, li {
			&:not(.scroll-keep) {
				transform-origin: center 0;
				perspective: 5rem;
				transition: all 100ms;
			}
		}
		li.scroll-keep {
			border-radius: var(--border-radius-m);
			background-color: #FFFFFF90;
			backdrop-filter: blur(0.25em);

			@media (prefers-color-scheme: dark) {
				background-color: #2C2C2C90;
			}
		}

		h1 {
			font-size: inherit;
			font-weight: inherit;
		}
		li {
			position: relative;
			padding: 0.5rem 0.75rem;
			pointer-events: auto;
			
			a:hover {
				filter: brightness(0.75);
			}
		}

		&-button--selection {
			position: relative;

			@include icon("chevron-l", 0.65em, "before");

			&:hover,
			&.active {
				color: var(--color-fg-dark);
				filter: none !important;

				&::before {
					filter: brightness(0) !important;

					@media (prefers-color-scheme: dark) {
						filter: brightness(1) !important;
					}
				}
			}

			&.active {
				@include icon("chevron-r", 0.65em, "before");

				// text-shadow: 0.25px 0 0 var(--color-fg-gray);
			}
			&.notify {
				&::after {
					$size: 0.6em;
					$borderWidth: 0.175em;

					content: "";
					position: absolute;
					top: calc($size * -0.275);
					right: -$size;
					width: $size;
					height: $size;
					background-color: var(--color-primary);
					border: $borderWidth solid var(--color-bg-light);
					border-radius: 100%;
					animation-name: notify;
					animation-duration: 800ms;
					animation-iteration-count: 1;

					@keyframes notify {
						0%, 50%, 100% {
							width: $size;
							height: $size;
							border-width: $borderWidth;
						}
						25%, 75% {
							width: calc($size + 2 * $borderWidth);
							height: calc($size + 2 * $borderWidth);
							border-width: 0;
						}
					}
				}
			}
		}
	}
	.hero {
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-top: calc(2.5 * var(--space-l));
        width: 100%;
        max-width: 42.5rem;
		text-align: center;

		&.uncollapse {
			.hero-logo {
				height: 7.75rem;
			}
		}

		&-logo {
			margin-bottom: calc(var(--space-l) - var(--space-s));
			height: 5rem;
			user-select: none;
			pointer-events: none;
		}
		&-description {
			@include font-l;

			margin-bottom: var(--space-l);
			color: var(--color-fg-gray);
			font-weight: 200;
		}
	}
</style>