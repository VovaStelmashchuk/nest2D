<template>
    <header>
        <div class="wrapper">
            <nav>
                <RouterLink to="/">Projects</RouterLink>
                <RouterLink to="/blog">Blog</RouterLink>
            </nav>
        </div>
    </header>
</template>
<script>
export default {
    name: 'AppHeader'
}
</script>
<style scoped>
header {
    padding: 0 2rem;
    height: 48px;
    line-height: 48px;
    width: 100%;
    position: fixed;
    top: 0;
    z-index: 10;
}

nav {
    margin: 0;
    padding: 0;
    width: 100%;
    font-size: 12px;
    text-align: center;
}

nav a {
    display: inline-block;
    padding: 0 2rem;
    border-left: 1px solid var(--color-border);
    font-size: 20px;
}

nav a:first-of-type {
    border: 0;
}

@media (min-width: 1024px) {
    header {
        display: flex;
        top: 0;
        place-items: center;
        padding-right: calc(var(--section-gap) / 2);
    }

    header .wrapper {
        width: 100%;
        display: flex;
        place-items: flex-start;
        flex-wrap: wrap;
    }

    nav {
        text-align: left;
        margin-left: -1rem;
        font-size: 2rem;

        padding: 1rem 0;
        margin-top: 1rem;
    }
}
</style>
