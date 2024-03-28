declare option output:method "xml";
declare option output:indent "yes";

<posts>{
  for $p in //posts/row
  order by $p/@ViewCount descending
  return <row>{$p/@Title, $p/@ViewCount}</row>
}</posts>
