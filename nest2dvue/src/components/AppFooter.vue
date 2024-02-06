<template>
    <footer>
        <div class="footer-wrapper">
            <nav>
                <RouterLink to="/terms-and-conditions">Terms and Conditions</RouterLink>
            </nav>
            <a href="https://github.com/VovaStelmashchuk/nest2D" target="_blank">Github</a>
            <div class="copy-right">
                © 2024 Vova Stelmashchuk. All rights reserved.
            </div>
        </div>
    </footer>
</template>

<script>
export default {
    name: 'AppFooter'
}
</script>

<style scoped>
.footer-wrapper {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
}

nav {
    display: flex;
}

nav a {
    margin-right: 20px;
    text-decoration: none;
    color: green;
}

.copy-right {
    font-size: 14px;
    color: #666;
}
</style>
