<template>
	<div class="placeholder">
		<div class="placeholder-bar placeholder-bar--l"></div>
		<div class="placeholder-bar"></div>
		<div class="placeholder-bar"></div>
	</div>
</template>

<style scoped lang="scss">
	@import "@/assets/scss/shared";
	
	.placeholder {
		filter: opacity(1);
		animation-name: fade-in;
		animation-duration: 600ms;
		animation-iteration-count: 1;

		@keyframes fade-in {
			0% {
				filter: opacity(0);
			}
			100% {
				filter: opacity(1);
			}
		}

		&-bar {
			--height: var(--font-size-m);

			margin-bottom: var(--space-m);
			width: 100%;
			max-width: 37.5rem !important;
			height: var(--height);
			background-color: var(--color-bg-gray);
			border-radius: calc(var(--height) / 2);
			animation-name: shine;
			animation-duration: 3s;
			animation-iteration-count: infinite;

			@keyframes shine {
				0%, 100% {
					background-color: var(--color-bg-gray);
				}
				50% {
					background-color: var(--color-fg-contrast);
				}
			}

			&--l {
				--height: calc(2 * var(--font-size-l));
				
				max-width: 25rem !important;
			}
		}
	}
</style>