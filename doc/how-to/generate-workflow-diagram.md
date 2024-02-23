#  Generate the markup for a status flow diagram

![status_flow](https://github.com/ministryofjustice/hmpps-accredited-programmes-api/assets/78762879/3e0a72ea-6914-47e6-a177-786ed2553a16)

Go to the endpoint `https://accredited-programmes-api-dev.hmpps.service.justice.gov.uk/status-transition-diagram`

paste the contents of the file into: http://magjac.com/graphviz-visual-editor/

or to generate an image save this as status_flow.dot

install graph viz: `brew install graphviz`

then run

`dot -Tpng status_flow.dot -o status_flow.png `

to save the image


