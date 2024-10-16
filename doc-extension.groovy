docinfo_processor {
    document -> {
        if (!document.basebackend('html') || !document.hasAttribute('fb-canonical-html')) {
            return
        }
        return "<link rel=\"canonical\" href=\"${document.getAttribute('fb-canonical-html')}\"/>"
    }
}
