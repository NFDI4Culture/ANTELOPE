<script setup lang="ts">
    import { Ref, ref, onMounted } from "vue";

    type TChangeHandler = (newIndex: number) => void;

    let defaultIndex = -1;

    const bodyEl = ref(null);
    const tabs: Ref<{
        desktopOnly: boolean;
        index: number;
        label: string;
    }[]> = ref([]);
    const activeTabIndex = ref(defaultIndex);

    const changeHandlers: TChangeHandler[] = [];

    onMounted(() => {
        const bodies: HTMLElement[] = Array.from(bodyEl.value.children);

        bodies
        .forEach((child: HTMLElement, i: number) => {
            const desktopOnly: boolean = child.classList.contains("desktop");

            tabs.value.push({
                desktopOnly,
                index: i,
                label: child.getAttribute("tab-label") ?? ""
            });
            
            defaultIndex = (
                defaultIndex < 0
                && (
                    !desktopOnly
                    || window.innerWidth >= parseInt(getComputedStyle(document.documentElement).getPropertyValue("--breakpoint"))
                )
            ) ? i : defaultIndex;
        });

        activateTab(defaultIndex);
    });

    function activateTab(newIndex: number = defaultIndex) {
        const bodies: HTMLElement[] = Array.from(bodyEl.value.children);

        const index = bodies[newIndex] ? newIndex : defaultIndex; 

        bodies[activeTabIndex.value]
        && bodies[activeTabIndex.value].classList.remove("active");
        bodies[index]
        && bodies[index].classList.add("active");

        activeTabIndex.value = index;

        changeHandlers.forEach((callback: TChangeHandler) => callback(activeTabIndex.value));
    }
    
    defineExpose({
        onTabChange(callback: TChangeHandler) {
            changeHandlers.push(callback);
        },
        getIndex(): number {
            return activeTabIndex.value;
        },
        activateTab
    });
</script>

<template>
    <div class="tabs">
        <ol class="tabs-head">
            <li v-for="tab in tabs" :key="tab.label" :class="tab.desktopOnly ? 'desktop' : ''">
                <a :class="(activeTabIndex === tab.index) ? 'active' : ''" @click="activateTab(tab.index)">{{ tab.label }}</a>
            </li>
        </ol>
        <div class="tabs-body" ref="bodyEl">
            <slot></slot>
        </div>
    </div>
</template>

<style scoped lang="scss">
	@import "@/assets/scss/shared";

    .tabs {
        width: 100%;

        &-head {
            list-style: none;
            align-items: flex-end;
            padding: 0;
            // border-bottom: 1px solid var(--color-fg-contrast);

            @include flex;
            
            li {
                margin-bottom: 0;
                height: fit-content;
                user-select: none;

                a {
                    display: block;
                    padding: var(--space-xs);
                    color: inherit;
                    text-align: center;
                    text-transform: uppercase;
                    border-bottom: 0.45rem solid /* transparent */var(--color-bg-gray);

                    &:hover {
                        border-bottom-color: var(--color-primary-subtle);
                    }

                    &.active {
                        font-weight: 600;
                        border-bottom-color: var(--color-primary);
                    }
                }
            }
        }
        &-body {
            padding-top: var(--space-xs);
            
            &:deep(> *) {
                display: none;

                &.active {
                    display: block;
                }
            }
        }
    }
</style>