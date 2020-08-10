<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
<xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />

<xsl:template match="*">
    <xsl:element name="{translate(local-name(), $uppercase, $lowercase)}" namespace="{namespace-uri()}">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
</xsl:template>

<xsl:template match="@*">
    <xsl:attribute name="{translate(local-name(), $uppercase, $lowercase)}" namespace="{namespace-uri()}">
        <xsl:value-of select="."/>
    </xsl:attribute>
</xsl:template>

<xsl:template match="comment() | text() | processing-instruction()">
    <xsl:copy/>
</xsl:template>

</xsl:stylesheet>