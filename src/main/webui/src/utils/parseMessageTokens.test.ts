import { describe, expect, it } from 'vitest';
import { parseMessageTokens } from './parseMessageTokens';

describe('parseMessageTokens', () => {
  it('returns a single text segment when there are no tokens', () => {
    expect(parseMessageTokens('just plain text')).toEqual([{ type: 'text', content: 'just plain text' }]);
  });

  it('parses a card token with surrounding text', () => {
    expect(parseMessageTokens('plays [card:100266:Bum’s Rush] now')).toEqual([
      { type: 'text', content: 'plays ' },
      { type: 'card', id: '100266', name: 'Bum’s Rush', advanced: false },
      { type: 'text', content: ' now' },
    ]);
  });

  it('flags an advanced card token', () => {
    expect(parseMessageTokens('[card:201363:Theo Bell:adv]')).toEqual([
      { type: 'card', id: '201363', name: 'Theo Bell', advanced: true },
    ]);
  });

  it('keeps a colon inside a card name (backtracks past the optional :adv)', () => {
    expect(parseMessageTokens('[card:1:Foo: Bar]')).toEqual([
      { type: 'card', id: '1', name: 'Foo: Bar', advanced: false },
    ]);
  });

  it('parses discipline, (D) and style tokens', () => {
    expect(parseMessageTokens('gains [disc:pot] then [d] then [disc:POT] and [style:big]')).toEqual([
      { type: 'text', content: 'gains ' },
      { type: 'disc', code: 'pot' },
      { type: 'text', content: ' then ' },
      { type: 'daction' },
      { type: 'text', content: ' then ' },
      { type: 'disc', code: 'POT' },
      { type: 'text', content: ' and ' },
      { type: 'style', content: 'big' },
    ]);
  });

  it('handles adjacent tokens', () => {
    expect(parseMessageTokens('[card:1:A][card:2:B]')).toEqual([
      { type: 'card', id: '1', name: 'A', advanced: false },
      { type: 'card', id: '2', name: 'B', advanced: false },
    ]);
  });

  it('leaves an unresolved bracket as text', () => {
    expect(parseMessageTokens('this [Some Name] did not resolve')).toEqual([
      { type: 'text', content: 'this [Some Name] did not resolve' },
    ]);
  });
});
