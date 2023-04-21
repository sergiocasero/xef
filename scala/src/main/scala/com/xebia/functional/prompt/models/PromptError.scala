package com.xebia.functional.prompt.models

import scala.util.control.NoStackTrace

class PromptError(reason: String) extends Throwable with NoStackTrace:
  override def getMessage(): String = s"Error generating Prompt: `$reason`"