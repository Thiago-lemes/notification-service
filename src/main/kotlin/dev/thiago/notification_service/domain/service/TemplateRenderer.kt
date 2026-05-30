package dev.thiago.notification_service.domain.service

import dev.thiago.notification_service.domain.model.RenderedTemplate
import dev.thiago.notification_service.domain.model.Template

object TemplateRenderer {

    fun render(template: Template, variables: Map<String, Any>): RenderedTemplate {
        return RenderedTemplate(
            subject = template.subject?.interpolate(variables),
            body = template.body.interpolate(variables)
        )
    }

    private fun String.interpolate(variables: Map<String, Any>): String {
        var result = this
        variables.forEach { (key, value) ->
            result = result.replace("{{$key}}", value.toString())
        }
        return result
    }
}