<template>
    <header class="header">
        <nav class="header__nav header-nav">
            <RouterLink
                to="/"
                class="header-nav__item"
            >
                Projects
            </RouterLink>
            <RouterLink
                to="/blog"
                class="header-nav__item"
            >
                Blog
            </RouterLink>
        </nav>
    </header>
</template>
<script>
export default {
    name: 'Header',
};
</script>
<style lang="scss" scoped>
.header {
    background: var(--color-background);
}
.header-nav {
    display: flex;

    &__item {
        color: var(--color-text);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-grow: 1;
        padding: 1rem 2rem;
    }

    @media (min-width: 1024px) {
        font-size: 2rem;
    }
}
</style>
