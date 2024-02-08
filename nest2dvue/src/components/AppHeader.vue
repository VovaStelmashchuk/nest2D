<template>
    <header>
        <nav>
            <a href="/" class="nav-item">Projects</a>
            <a href="/Blog" class="nav-item">Blog</a>
        </nav>
    </header>
</template>
<script>
export default {
    name: 'AppHeader'
}
</script>
<style scoped>

header {
    height: 48px;
    width: 100%;
    position: fixed;
    top: 0;
    z-index: 10;
    margin: 0;
    padding: 0;
    justify-content: center;
    background: var(--color-background);
}

nav {
    width: 100%;
    background: var(--color-background);
    display: flex;
    gap: 20px;
    justify-content: center;
}

.nav-item {
    display: flex;
    align-items: center;
    justify-content: center;
    text-decoration: none;
    color: inherit;
    flex-grow: 1;
    padding: 1rem 2rem;
}

@media (min-width: 1024px) {
    header {
        display: flex;
        top: 0;
        place-items: center;
        padding-right: calc(var(--section-gap) / 2);
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
