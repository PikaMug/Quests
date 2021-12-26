name: Report a bug
description: when something isn't working right.
labels: bug
body:
  - type: markdown
    attributes:
      value: |
        Thanks for submitting a bug report! Please answer the following questions:
  - type: 'textarea'
    attributes:
      label: 'What is your Quests version / build number (do _not_ say "latest")?'
      description: 'You can find this using the `/quests info` command.'
      placeholder: 'The version used is...'
    validations:
      required: true
  - type: 'textarea'
    attributes:
      label: 'Describe the problem and how to replicate it. Any console errors to post _using a paste service_?'
      description: 'Popular services include Pastebin and Hastebin.'
      placeholder: 'A problem I found is...'
    validations:
      required: true
  - type: 'textarea'
    attributes:
      label: 'If applicable, can you provide an example quest from quests.yml _using a paste service_?'
      description: 'Do not upload the file itself.'
      placeholder: 'A problem I found is...'
    validations:
      required: false
