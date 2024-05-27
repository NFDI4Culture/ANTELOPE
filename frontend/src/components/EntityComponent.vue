<template>
    <div class="entity">
        <span>
            <slot name="label"></slot>
        </span>
        <em>
            <slot name="id"></slot>
        </em>
    </div>
</template>

<style scoped lang="scss">
	@import "@/assets/scss/shared";
    
    .entity {
        display: flex;
        flex-direction: row;
        align-items: center;
                
        span, em {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        em {
            margin-left: 0.5em;
            color: var(--color-fg-gray);
            font-weight: 200;
        }
    }
</style>