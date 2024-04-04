declare option output:method "xml";
declare option output:indent "yes";

<Posts>{
  for $p in /posts/row[@PostTypeId = 1]
  order by xs:integer($p/@ViewCount) descending
  return <Post>{
    <Title>{$p/@Title}</Title>,
    <ViewCount>{$p/@ViewCount}</ViewCount>
  }</Post>
}</Posts>
