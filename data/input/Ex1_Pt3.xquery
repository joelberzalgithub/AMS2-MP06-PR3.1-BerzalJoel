declare option output:method "xml";
declare option output:indent "yes";

<tags>{
  for $t in //tags/row
  order by $t/@Count descending
  return <row>{$t/@TagName, $t/@Count}</row>
}</tags>
