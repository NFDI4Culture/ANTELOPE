export class ScrollAbilityService {
	private static isScrollable: boolean = true;

	public static toggleScroll(hard = false) {
		!ScrollAbilityService.isScrollable ? ScrollAbilityService.enableScroll(hard) : ScrollAbilityService.disableScroll(hard);
	}

	public static enableScroll(hard = false) {
		ScrollAbilityService.isScrollable = true;

		if (hard) {
			document.body.classList.remove("no-scroll");

			return;
		}

		window.removeEventListener("selectstart", e => e.preventDefault());

		window.onscroll = () => {};
	}

	public static disableScroll(hard = false) {
		ScrollAbilityService.isScrollable = false;

		if (hard) {
			document.body.classList.add("no-scroll");

			return;
		}

		window.addEventListener("selectstart", e => e.preventDefault());

		const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
		const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft;

		window.onscroll = e => {
			e.preventDefault();

			window.scrollTo(scrollLeft, scrollTop);
		};
	}
}
