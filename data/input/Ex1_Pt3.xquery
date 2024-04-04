declare option output:method "xml";
declare option output:indent "yes";

<Tags>{
  for $t in /tags/row
  order by xs:integer($t/@Count) descending
  return <Tag>{
    <TagName>{$t/@TagName}</TagName>,
    <Count>{$t/@Count}</Count>
  }</Tag>
}</Tags>
