declare option output:method "xml";
declare option output:indent "yes";

<Posts>{
  let $posts := /posts/row[@PostTypeId = 1]  
  let $sortedPosts := $posts
                          order by xs:integer(@Score) descending 
  for $post in subsequence($sortedPosts, 1, 100)
  let $answers := /posts/row[@PostTypeId = 2 and @ParentId = $post/@Id]
  let $sortedAnswers := $answers
                        order by xs:integer(@Score) descending 
  return <Post>{
      <Title>{$post/@Title}</Title>,
      <Body>{$post/@Body}</Body>,
      <Score>{$post/@Score}</Score>,
      <Tags>{$post/@Tags}</Tags>,
      <MostVotedAnswer>{
          for $answer in subsequence($sortedAnswers, 1, 1)
          return <Answer>{
              <Body>{$answer/@Body}</Body>,
              <Score>{$answer/@Score}</Score>
            }</Answer>
        }</MostVotedAnswer>
  }</Post>
}</Posts>
